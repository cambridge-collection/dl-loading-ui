package uk.cam.lib.cdl.loading.apis;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Preconditions;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import uk.cam.lib.cdl.loading.config.GitLocalVariables;
import uk.cam.lib.cdl.loading.model.editor.Collection;
import uk.cam.lib.cdl.loading.model.editor.Dataset;
import uk.cam.lib.cdl.loading.model.editor.Id;
import uk.cam.lib.cdl.loading.model.editor.Item;
import uk.cam.lib.cdl.loading.model.editor.UI;
import uk.cam.lib.cdl.loading.model.editor.ui.UICollection;
import uk.cam.lib.cdl.loading.utils.GitHelper;

import javax.validation.constraints.NotNull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
TODO This should be refactored out to talk to a external API.
Access info from the git data directly for now.
*/
public class EditAPI {

    private final Path dataPath;
    private final Path dataItemPath;
    private final Path datasetFile;
    private final Path uiFile;
    private final GitHelper gitHelper;

    private Map<String, Collection> collectionMap = Collections.synchronizedMap(new HashMap<>());
    private Map<String, Path> collectionFilepaths = Collections.synchronizedMap(new HashMap<>());
    private Map<String, Item> itemMap = Collections.synchronizedMap(new HashMap<>());
    private Map<String, String> thumbnailImageURLs = Collections.synchronizedMap(new HashMap<>());
    private final Pattern filenamePattern = Pattern.compile("^[a-zA-Z0-9]+-[a-zA-Z0-9]+[a-zA-Z0-9\\-]*-[0-9]{5}$");

    public EditAPI(String dataPath, String dlDatasetFilename, String dlUIFilename, String dataItemPath,
                   GitLocalVariables gitSourceVariables) {
        this(dataPath, dlDatasetFilename, dlUIFilename, dataItemPath, new GitHelper(gitSourceVariables));
    }

    /**
     * Used for testing Edit API
     *
     * @param dataPath          File path to source data
     * @param dlDatasetFilename Filename for the root JSON file for this dataset.
     * @param dataItemPath      File path to the item TEI directory.
     * @param gitHelper         Object for interacting with Git
     */
    public EditAPI(String dataPath, String dlDatasetFilename, String dlUIFilename, String dataItemPath,
                   GitHelper gitHelper) {
        this.gitHelper = gitHelper;
        this.dataPath = Path.of(dataPath).normalize();
        Preconditions.checkArgument(this.dataPath.isAbsolute(), "dataPath is not absolute: %s", dataPath);
        this.datasetFile = this.dataPath.resolve(dlDatasetFilename).normalize();
        this.uiFile = this.dataPath.resolve(dlUIFilename).normalize();
        this.dataItemPath = Path.of(dataItemPath).normalize();
        Preconditions.checkArgument(this.dataItemPath.startsWith(this.dataPath), "dataItemPath is not under dataPath");
        setup();
    }

    private void setup() {
        try {
            if (!Files.exists(datasetFile)) {
                throw new FileNotFoundException("Dataset file cannot be found at: " + datasetFile);
            }

            updateModel();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Re-reads data from the file system and converts this into the object Model.
     * Helpful to update from Git changes made elsewhere.
     *
     * @throws IOException On problems accessing file system data
     */
    public synchronized void updateModel() throws IOException {
        try {
            gitHelper.pullGitChanges();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
        mapper.enable(JsonParser.Feature.ALLOW_TRAILING_COMMA);
        Dataset dataset = mapper.readValue(datasetFile.toFile(), Dataset.class);
        Map<String, Collection> newCollectionMap = Collections.synchronizedMap(new HashMap<>());
        Map<String, Path> newCollectionFilepaths = Collections.synchronizedMap(new HashMap<>());
        Map<String, Item> newItemMap = Collections.synchronizedMap(new HashMap<>());

        // Setup collections
        for (Id id : dataset.getCollections()) {

            String collectionId = id.getId();
            var collectionFile = dataPath.resolve(collectionId).normalize();
            Preconditions.checkState(collectionFile.startsWith(dataPath), "Collection '%s' is not under dataPath", collectionId);
            if (!Files.exists(collectionFile)) {
                throw new FileNotFoundException("Collection file cannot be found at: " + collectionFile);
            }

            Collection c = mapper.readValue(collectionFile.toFile(), Collection.class);
            c.setCollectionId(collectionId);

            // Setup collection maps
            newCollectionMap.put(collectionId, c);
            newCollectionFilepaths.put(collectionId, collectionFile);

            for (Id itemId : c.getItemIds()) {
                newItemMap.put(FilenameUtils.getBaseName(itemId.getId()), createItem(itemId.getId(), collectionFile));
            }
        }

        this.collectionMap = newCollectionMap;
        this.collectionFilepaths = newCollectionFilepaths;
        this.itemMap = newItemMap;

        // Update UI
        Map<String, String> newThumbnailImageURLs = Collections.synchronizedMap(new HashMap<>());
        UI ui = mapper.readValue(uiFile.toFile(), UI.class);
        for (UICollection collection : ui.getThemeData().getCollections()) {

            // Get collection Id from the filepath
            String collectionId = collection.getCollection().getId();

            // TODO Properly format Ids into set pattern
            // For now remove ./ at the start of a filepath
            if (collectionId.startsWith("./")) {
                collectionId = collectionId.replaceFirst("./", "");
            }

            newThumbnailImageURLs.put(collectionId, collection.getThumbnail().getId());

        }
        this.thumbnailImageURLs = newThumbnailImageURLs;

        for (Collection c : collectionMap.values()) {
            String thumbnailURL = getCollectionThumbnailURL(c.getCollectionId());
            c.setThumbnailURL(thumbnailURL);
        }

    }

    /**
     * Get the ThumbnailURL for a collection.
     *
     * @param collectionId relative to the ui file.
     * @return
     */
    private String getCollectionThumbnailURL(@NotNull String collectionId) {

        return this.thumbnailImageURLs.get(collectionId);
    }

    private void setCollectionThumbnailURL(String thumbnailURL, String collectionId) {

        try {
            ObjectMapper mapper = new ObjectMapper();

            mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
            mapper.enable(JsonParser.Feature.ALLOW_TRAILING_COMMA);
            UI ui = mapper.readValue(uiFile.toFile(), UI.class);
            for (UICollection collection : ui.getThemeData().getCollections()) {
                String thisCollectionPath = collection.getCollection().getId();

                if (thisCollectionPath.equals(collectionId)) {

                    collection.setThumbnail(new Id(thumbnailURL));
                    ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
                    writer.writeValue(uiFile.toFile(), ui);
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public List<Collection> getCollections() {
        return new ArrayList<>(collectionMap.values());
    }

    public Collection getCollection(String collectionId) {
        return collectionMap.get(collectionId);
    }

    private Item createItem(String id, Path collectionFile) throws IOException {
        var itemFile = collectionFile.getParent().resolve(id).normalize();
        return new Item(FilenameUtils.getBaseName(itemFile.getFileName().toString()), itemFile, new Id(id));
    }

    public Item getItem(String id) {
        return itemMap.get(FilenameUtils.getBaseName(id));
    }

    public boolean validate(MultipartFile file) {

        // Validate content
        boolean valid = false;
        try {
            String content = IOUtils.toString(file.getInputStream(), StandardCharsets.UTF_8);

            if (Objects.requireNonNull(file.getContentType()).equals("text/xml") ||
                Objects.requireNonNull(file.getContentType()).equals("application/xml")) {
                valid = validateXML(file.getOriginalFilename(), content);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return valid;
    }

    public boolean validateFilename(String filename) {

        Matcher matcher = filenamePattern.matcher(filename);
        return matcher.find();

    }

    private boolean validateXML(String filename, String content) {

        // TODO There currently does not exists a XML schema to validate against.
        // could convert to JSON using cudl-pack and then validate against item json?

        // For now, just make sure it's valid XML.
        DocumentBuilder parser = null;
        try {
            parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = parser.parse(IOUtils.toInputStream(content, "UTF-8"));
            if (document != null) {
                return true;
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private Path getDataItemPath() {
        return dataItemPath;
    }

    private boolean itemInCollection(String itemName, Collection collection) {
        Item item = itemMap.get(itemName);
        if (item != null) {
            return collection.getItemIds().contains(item.getId());
        }
        return false;
    }

    public boolean addItemToCollection(String itemName, String fileExtension, InputStream contents, String collectionId) {

        try {
            gitHelper.pullGitChanges();

            Collection collection = getCollection(collectionId);

            Path output;

            // Check to see if the file already exists
            boolean itemAlreadyInCollection = itemInCollection(itemName, collection);
            if (itemMap.containsKey(itemName)) {

                // Overwrite existing Item
                output = itemMap.get(itemName).getFilepath();

            } else {

                // new Item
                output = dataItemPath.resolve(FilenameUtils.getBaseName(itemName)).resolve(itemName + "." + fileExtension);
                Files.createDirectories(output.getParent());

            }

            // Write out file.
            FileUtils.copyInputStreamToFile(contents, output.toFile());

            if (!itemAlreadyInCollection) {

                Path collectionFilePath = Preconditions.checkNotNull(
                    collectionFilepaths.get(collection.getCollectionId()));
                // Add itemId to collection
                if (itemMap.containsKey(itemName)) {
                    collection.getItemIds().add(itemMap.get(itemName).getId());
                } else {
                    Path collectionDir = collectionFilePath.getParent();
                    Path itemPath = collectionDir.relativize(output);
                    Id id = new Id(itemPath.toString());
                    collection.getItemIds().add(id);
                }

                // Write out collection file
                ObjectMapper mapper = new ObjectMapper();
                ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
                writer.writeValue(collectionFilePath.toFile(), collection);

            }

            gitHelper.pushGitChanges();

            updateModel();

            return true;

        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteItemFromCollection(String itemName, String collectionId) {

        try {
            gitHelper.pullGitChanges();

            Collection collection = getCollection(collectionId);
            Item item = itemMap.get(itemName);
            collection.getItemIds().remove(item.getId());

            // Write out collection file
            ObjectMapper mapper = new ObjectMapper();
            ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
            Path collectionFilePath = Preconditions.checkNotNull(collectionFilepaths.get(collectionId));
            writer.writeValue(collectionFilePath.toFile(), collection);

            // Delete file for item if it exists in no other collection
            if (getFirstCollectionForItem(item) == null) {
                var f = item.getFilepath();
                if (!Files.deleteIfExists(f)) {
                    return false;
                }
                // remove parent dir if empty
                var parentFile = f.getParent();
                if (Files.isDirectory(parentFile)) {
                    try {
                        Files.deleteIfExists(parentFile);
                    }
                    catch (DirectoryNotEmptyException e) { /* ignore */ }
                }
            }

            gitHelper.pushGitChanges();

            updateModel();

            return true;
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Collection getFirstCollectionForItem(Item i) {

        for (Collection c : getCollections()) {
            if (c.getItemIds().contains(i.getId())) {
                return c;
            }
        }
        return null;
    }

    public boolean updateCollection(Collection collection, String descriptionHTML, String creditHTML) {
        try {

            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
            mapper.enable(JsonParser.Feature.ALLOW_TRAILING_COMMA);
            ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

            String collectionId = collection.getCollectionId();

            Path collectionFilepath = collectionFilepaths.get(collectionId);
            if (collectionFilepath == null) {
                // New item so create filepath
                collectionFilepath = dataPath.resolve(collectionId).normalize();
                Preconditions.checkState(collectionFilepath.startsWith(dataPath), "collection path is not under dataPath: '%s'", collectionFilepath);
            }

            // If collection is not in dataset file add it.
            Dataset dataset = mapper.readValue(datasetFile.toFile(), Dataset.class);
            List<Id> collectionIds = dataset.getCollections();
            if (!collectionIds.contains(new Id(collectionId))) {
                collectionIds.add(new Id(collectionId));
                writer.writeValue(datasetFile.toFile(), dataset);
            }

            // If collectionThumbnail is not in uiFile add it
            // TODO: Allow settings or collection form to define UI layout for new collections
            UI ui = mapper.readValue(uiFile.toFile(), UI.class);
            List<UICollection> uiCollections = ui.getThemeData().getCollections();
            if (!UICollectionContains(collectionId, uiCollections)) {
                uiCollections.add(new UICollection(new Id(collectionId),
                    "organisation", new Id(collection.getThumbnailURL())));
                writer.writeValue(uiFile.toFile(), ui);
            }

            // Write out collection file
            writer.writeValue(collectionFilepath.toFile(), collection);

            // Write out HTML section files
            var descriptionHTMLFile = collectionFilepath.getParent().resolve(collection.getDescription().getFull().getId()).normalize();
            Preconditions.checkState(descriptionHTMLFile.startsWith(dataPath), "descriptionHTMLFile is not under dataPath: %s", descriptionHTMLFile);
            FileUtils.write(descriptionHTMLFile.toFile(), descriptionHTML, "UTF-8");

            var creditHTMLFile = collectionFilepath.getParent().resolve(collection.getCredit().getProse().getId()).normalize();
            Preconditions.checkState(creditHTMLFile.startsWith(dataPath), "creditHTMLFile is not under dataPath: %s", creditHTMLFile);
            FileUtils.write(creditHTMLFile.toFile(), creditHTML, "UTF-8");

            // Update collection thumbnail in the UI
            setCollectionThumbnailURL(collection.getThumbnailURL(), collectionId);

            // Git Commit and push to remote repo.
            boolean success = gitHelper.pushGitChanges();
            updateModel();

            return success;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Path getDataLocalPath() {
        var result = Path.of(gitHelper.getDataLocalPath());
        Preconditions.checkState(result.isAbsolute() && result.normalize().equals(result),
            "gitHelper.getDataLocalPath() is not a normalised absolute path: %s", result);
        return result;
    }

    public Path getCollectionPath(String collectionId) {
        return collectionFilepaths.get(collectionId);
    }

    public Path getDatasetFile() {
        return datasetFile;
    }

    private boolean UICollectionContains(String collectionId, List<UICollection> uiCollections) {
        for (UICollection c : uiCollections) {
            if (c.getCollection().equals(new Id(collectionId))) {
                return true;
            }
        }
        return false;
    }
}

