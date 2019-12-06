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
     * For testing
     *
     * @param dataPath
     * @param dlDatasetFilename
     * @param dataItemPath
     * @param gitHelper
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

            String collectionPath = id.getId();
            File collectionFile = new File(dataPath + File.separator + collectionPath);
            if (!collectionFile.exists()) {
                throw new FileNotFoundException("Collection file cannot be found at: " + collectionFile.toPath());
            }

            Collection c = mapper.readValue(collectionFile, Collection.class);
            String urlSlug = c.getName().getUrlSlug();
            String thumbnailURL = getCollectionThumbnailURL(collectionFile.getCanonicalPath());
            c.setThumbnailURL(thumbnailURL);

            // Setup collection maps
            newCollectionMap.put(urlSlug, c);
            newCollectionFilepaths.put(urlSlug, collectionFile.getCanonicalPath());

            for (Id itemId : c.getItemIds()) {
                newItemMap.put(FilenameUtils.getBaseName(itemId.getId()), createItem(itemId.getId(), collectionFile));
            }

        }

        this.collectionMap = newCollectionMap;
        this.collectionFilepaths = newCollectionFilepaths;
        this.itemMap = newItemMap;
    }

    // TODO parse UI data into a model object
    private String getCollectionThumbnailURL(String requiredCollectionFilepath) {

        if (requiredCollectionFilepath != null) {
            try {

                ObjectMapper mapper = new ObjectMapper();
                mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
                mapper.enable(JsonParser.Feature.ALLOW_TRAILING_COMMA);
                UI ui = mapper.readValue(uiFile, UI.class);
                for (UICollection collection : ui.getThemeData().getCollections()) {
                    String collectionPath = collection.getCollection().getId();
                    File collectionFile = new File(dataPath + File.separator + collectionPath);
                    if (collectionFile.getCanonicalPath().equals(requiredCollectionFilepath)) {
                        String relativeThumbnailPath = collection.getThumbnail().getId();
                        File thumbnailFile = new File(dataPath, relativeThumbnailPath);
                        return thumbnailFile.getCanonicalPath();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public List<Collection> getCollections() {
        return new ArrayList<>(collectionMap.values());
    }

    public Collection getCollection(String urlSlug) {
        return collectionMap.get(urlSlug);
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

    public boolean addItemToCollection(String itemName, String fileExtension, InputStream contents, String collectionUrlSlug) {

        try {
            gitHelper.pullGitChanges();

            Collection collection = getCollection(collectionUrlSlug);

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

                String collectionFilePath = collectionFilepaths.get(collection.getName().getUrlSlug());
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

    public boolean deleteItemFromCollection(String itemName, String collectionUrlSlug) {

        try {
            gitHelper.pullGitChanges();

            Collection collection = getCollection(collectionUrlSlug);
            Item item = itemMap.get(itemName);
            collection.getItemIds().remove(item.getId());

            // Write out collection file
            ObjectMapper mapper = new ObjectMapper();
            ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
            String collectionFilePath = collectionFilepaths.get(collectionUrlSlug);
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

    public boolean updateCollection(Collection collection) {
        try {

            final Set<String> collections = collectionMap.keySet();
            ObjectMapper mapper = new ObjectMapper();

            // New collection
            if (!collections.contains(collection)) {

                // add collection to dataset file.
                Dataset dataset = mapper.readValue(datasetFile, Dataset.class);
                List<Id> collectionIds = dataset.getCollections();
                // assume new collections go in the collections directory
                String collectionPath = "collections/" + collection.getName().getUrlSlug() + ".collection.json";
                collectionIds.add(new Id(collectionPath));

                // Write out dataset file
                ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
                writer.writeValue(datasetFile, dataset);

                File collectionFile = new File(dataPath + File.separator + collectionPath);
                collectionFilepaths.put(collection.getName().getUrlSlug(), collectionFile.getCanonicalPath());

            }

            // Write out collection file
            String collectionFilepath = collectionFilepaths.get(collection.getName().getUrlSlug());

            ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
            writer.writeValue(new File(collectionFilepath), collection);

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

    public File getDatasetFile() {
        return datasetFile;
    }

}

