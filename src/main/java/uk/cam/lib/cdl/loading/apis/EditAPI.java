package uk.cam.lib.cdl.loading.apis;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import uk.cam.lib.cdl.loading.config.GitLocalVariables;
import uk.cam.lib.cdl.loading.model.editor.Collection;
import uk.cam.lib.cdl.loading.model.editor.*;
import uk.cam.lib.cdl.loading.model.editor.ui.UICollection;
import uk.cam.lib.cdl.loading.utils.GitHelper;

import javax.validation.constraints.NotNull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
TODO This should be refactored out to talk to a external API.
Access info from the git data directly for now.
*/
public class EditAPI {

    private final String dataPath;
    private final String dataItemPath;
    private final File datasetFile;
    private final File uiFile;
    private final GitHelper gitHelper;

    private Map<String, Collection> collectionMap = Collections.synchronizedMap(new HashMap<>());
    private Map<String, String> collectionFilepaths = Collections.synchronizedMap(new HashMap<>());
    private Map<String, Item> itemMap = Collections.synchronizedMap(new HashMap<>());
    private Map<String, String> thumbnailImageURLs = Collections.synchronizedMap(new HashMap<>());
    private final Pattern filenamePattern = Pattern.compile("^[a-zA-Z0-9]+-[a-zA-Z0-9]+[a-zA-Z0-9\\-]*-[0-9]{5}$");

    public EditAPI(String dataPath, String dlDatasetFilename, String dlUIFilename, String dataItemPath,
                   GitLocalVariables gitSourceVariables) {
        this.gitHelper = new GitHelper(gitSourceVariables);
        this.dataPath = dataPath;
        this.datasetFile = new File(dataPath + File.separator + dlDatasetFilename);
        this.uiFile = new File(dataPath + File.separator + dlUIFilename);
        this.dataItemPath = dataItemPath;
        setup();
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
        this.dataPath = dataPath;
        this.datasetFile = new File(dataPath + File.separator + dlDatasetFilename);
        this.uiFile = new File(dataPath + File.separator + dlUIFilename);
        this.dataItemPath = dataItemPath;
        setup();
    }

    private void setup() {
        try {
            if (!datasetFile.exists()) {
                throw new FileNotFoundException("Dataset file cannot be found at: " + datasetFile.toPath());
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
        Dataset dataset = mapper.readValue(datasetFile, Dataset.class);
        Map<String, Collection> newCollectionMap = Collections.synchronizedMap(new HashMap<>());
        Map<String, String> newCollectionFilepaths = Collections.synchronizedMap(new HashMap<>());
        Map<String, Item> newItemMap = Collections.synchronizedMap(new HashMap<>());

        // Setup collections
        for (Id id : dataset.getCollections()) {

            String collectionId = id.getId();
            File collectionFile = new File(dataPath + File.separator + collectionId);
            if (!collectionFile.exists()) {
                throw new FileNotFoundException("Collection file cannot be found at: " + collectionFile.toPath());
            }

            Collection c = mapper.readValue(collectionFile, Collection.class);
            c.setCollectionId(collectionId);

            // Setup collection maps
            newCollectionMap.put(collectionId, c);
            newCollectionFilepaths.put(collectionId, collectionFile.getCanonicalPath());

            for (Id itemId : c.getItemIds()) {
                newItemMap.put(FilenameUtils.getBaseName(itemId.getId()), createItem(itemId.getId(), collectionFile));
            }

        }

        this.collectionMap = newCollectionMap;
        this.collectionFilepaths = newCollectionFilepaths;
        this.itemMap = newItemMap;

        // Update UI
        Map<String, String> newThumbnailImageURLs = Collections.synchronizedMap(new HashMap<>());
        UI ui = mapper.readValue(uiFile, UI.class);
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
            UI ui = mapper.readValue(uiFile, UI.class);
            for (UICollection collection : ui.getThemeData().getCollections()) {
                String thisCollectionPath = collection.getCollection().getId();

                if (thisCollectionPath.equals(collectionId)) {

                    collection.setThumbnail(new Id(thumbnailURL));
                    ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
                    writer.writeValue(uiFile, ui);
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

    private Item createItem(String id, File collectionFile) throws IOException {
        File f = new File(collectionFile.getParentFile().getCanonicalPath(), id);
        return new Item(FilenameUtils.getBaseName(f.getName()), f.getCanonicalPath(), new Id(id));
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

    private String getDataItemPath() {
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

            File output;

            // Check to see if the file already exists
            boolean itemAlreadyInCollection = itemInCollection(itemName, collection);
            if (itemMap.containsKey(itemName)) {

                // Overwrite existing Item
                output = new File(itemMap.get(itemName).getFilepath());

            } else {

                // new Item
                output = new File(getDataItemPath() +
                    FilenameUtils.getBaseName(itemName) +
                    File.separator + itemName + "." + fileExtension);
                output.getParentFile().mkdirs();

            }

            // Write out file.
            FileUtils.copyInputStreamToFile(contents, output);

            if (!itemAlreadyInCollection) {

                String collectionFilePath = collectionFilepaths.get(collection.getCollectionId());
                // Add itemId to collection
                if (itemMap.containsKey(itemName)) {
                    collection.getItemIds().add(itemMap.get(itemName).getId());
                } else {
                    String collectionDir = new File(collectionFilePath).getParentFile().getPath();
                    String itemPath = Paths.get(collectionDir).relativize(Paths.get(output.getCanonicalPath())).toString();
                    Id id = new Id(itemPath);
                    collection.getItemIds().add(id);
                }

                // Write out collection file
                ObjectMapper mapper = new ObjectMapper();
                ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
                writer.writeValue(new File(collectionFilePath), collection);

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
            String collectionFilePath = collectionFilepaths.get(collectionId);
            writer.writeValue(new File(collectionFilePath), collection);

            // Delete file for item if it exists in no other collection
            if (getFirstCollectionForItem(item) == null) {
                File f = new File(item.getFilepath());
                if (!f.delete()) {
                    return false;
                }
                // remove parent dir if empty
                File parentFile = f.getParentFile();
                if (parentFile != null && f.getParentFile().isDirectory() &&
                    Objects.requireNonNull(parentFile.list()).length == 0) {
                    if (!parentFile.delete()) {
                        return false;
                    }
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

            String collectionFilepath = collectionFilepaths.get(collectionId);
            if (collectionFilepath == null) {
                // New item so create filepath
                collectionFilepath = dataPath + File.separator + collectionId;
            }

            // If collection is not in dataset file add it.
            Dataset dataset = mapper.readValue(datasetFile, Dataset.class);
            List<Id> collectionIds = dataset.getCollections();
            if (!collectionIds.contains(new Id(collectionId))) {
                collectionIds.add(new Id(collectionId));
                writer.writeValue(datasetFile, dataset);
            }

            // If collectionThumbnail is not in uiFile add it
            // TODO: Allow settings or collection form to define UI layout for new collections
            UI ui = mapper.readValue(uiFile, UI.class);
            List<UICollection> uiCollections = ui.getThemeData().getCollections();
            if (!UICollectionContains(collectionId, uiCollections)) {
                uiCollections.add(new UICollection(new Id(collectionId),
                    "organisation", new Id(collection.getThumbnailURL())));
                writer.writeValue(uiFile, ui);
            }

            // Write out collection file
            File collectionFile = new File(collectionFilepath);
            writer.writeValue(collectionFile, collection);

            // Write out HTML section files
            File descriptionHTMLFile = new File(collectionFile.getParent(), collection.getDescription().getFull().getId());
            FileUtils.write(descriptionHTMLFile, descriptionHTML, "UTF-8");

            File creditHTMLFile = new File(collectionFile.getParent(), collection.getCredit().getProse().getId());
            FileUtils.write(creditHTMLFile, creditHTML, "UTF-8");

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

    public String getDataLocalPath() {
        return gitHelper.getDataLocalPath();
    }

    public String getCollectionPath(String collectionId) {
        return collectionFilepaths.get(collectionId);
    }

    public File getDatasetFile() {
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

