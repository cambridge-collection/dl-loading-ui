package uk.cam.lib.cdl.loading.apis;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import uk.cam.lib.cdl.loading.config.GitVariables;
import uk.cam.lib.cdl.loading.model.editor.Collection;
import uk.cam.lib.cdl.loading.model.editor.Dataset;
import uk.cam.lib.cdl.loading.model.editor.Id;
import uk.cam.lib.cdl.loading.model.editor.Item;

import javax.annotation.PostConstruct;
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

    @Autowired
    private GitVariables gitVariables;

    private Git git;
    private final String dataPath;
    private final String dataItemPath;
    private final File datasetFile;

    private Dataset dataset;
    private List<Collection> collections = new ArrayList<>();
    private Map<String, Collection> collectionMap = new HashMap<>();
    private Map<String, Item> itemMap = new HashMap<>();
    private final Pattern filenamePattern = Pattern.compile("^[a-zA-Z0-9]+-[a-zA-Z0-9]+[a-zA-Z0-9\\-]*-[0-9]{5}$");


    public EditAPI(String dataPath, String dlDatasetFilename, String dataItemPath) {
        this.dataPath = dataPath;
        this.datasetFile = new File(dataPath + File.separator + dlDatasetFilename);
        this.dataItemPath = dataItemPath;
    }

    @PostConstruct
    private void setupEditAPI() throws IOException {
        // Clone git repo if not already available.
        setupRepo(gitVariables.getGitSourcePath(), gitVariables.getGitSourceURL(), gitVariables.getGitSourceURLUserame(),
            gitVariables.getGitSourceURLPassword());

        if (!datasetFile.exists()) {
            throw new FileNotFoundException("Dataset file cannot be found at: " + datasetFile.toPath());
        }

        updateModel();
    }

    public void updateModel() throws IOException {

        try {
            pullGitChanges();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        dataset = objectMapper.readValue(datasetFile, Dataset.class);
        List<Collection> newCollections = new ArrayList<>();
        Map<String, Collection> newCollectionMap = new HashMap<>();
        Map<String, Item> newItemMap = new HashMap<>();

        // Setup collections
        for (Id id : dataset.getCollections()) {

            String collectionPath = id.getId();

            File collectionFile = new File(dataPath + File.separator + collectionPath);
            if (!collectionFile.exists()) {
                throw new FileNotFoundException("Collection file cannot be found at: " + collectionFile.toPath());
            }

            Collection c = objectMapper.readValue(collectionFile, Collection.class);
            c.setFilepath(collectionFile.getCanonicalPath()); // is needed to get correct item path
            c.setThumbnailURL(getDataLocalPath() + "/pages/images/collectionsView/collection-" + c.getName().getUrlSlug() +
                ".jpg"); // TODO fix hardcoding
            newCollections.add(c);

            // Setup collection map
            newCollectionMap.put(c.getName().getUrlSlug(), c);

            // Items
            for (Id itemId : c.getItemIds()) {
                newItemMap.put(FilenameUtils.getBaseName(itemId.getId()), createItem(itemId.getId(), c));
            }

        }

        this.collections = newCollections;
        this.collectionMap = newCollectionMap;
        this.itemMap = newItemMap;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public List<Collection> getCollections() {
        return collections;
    }

    public Collection getCollection(String urlSlug) {
        return collectionMap.get(urlSlug);
    }

    private Item createItem(String id, Collection collection) throws IOException {

        File collectionFile = new File(collection.getFilepath());
        File f = new File(collectionFile.getParentFile().getCanonicalPath(), id);
        return new Item(FilenameUtils.getBaseName(f.getName()), f.getCanonicalPath(), new Id(id));
    }

    public Item getItem(String id) {
        return itemMap.get(FilenameUtils.getBaseName(id));
    }

    private void setupRepo(String gitSourcePath, String gitSourceURL, String gitSourceURLUserame, String gitSourceURLPassword) {
        try {
            File dir = new File(gitSourcePath);
            if (dir.exists()) {

                git = Git.init().setDirectory(dir).call();

            } else {

                git = Git.cloneRepository()
                    .setURI(gitSourceURL)
                    .setBranch(gitVariables.getGitBranch())
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitSourceURLUserame,
                        gitSourceURLPassword))
                    .setDirectory(new File(gitSourcePath))
                    .call();

            }

        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns true if there were any changes.
     * Runs on a schedule form EditConfig
     *
     * @return
     * @throws GitAPIException
     * @throws IOException
     */
    private boolean pullGitChanges() throws GitAPIException {

        FetchResult fetchResult = git.fetch().setCredentialsProvider(
            new UsernamePasswordCredentialsProvider(gitVariables.getGitSourceURLUserame(),
                gitVariables.getGitSourceURLPassword())).call();

        // Check for changes, and pull if there have been.
        if (!fetchResult.getTrackingRefUpdates().isEmpty()) {
            PullResult pullResult = git.pull().setCredentialsProvider(
                new UsernamePasswordCredentialsProvider(gitVariables.getGitSourceURLUserame(),
                    gitVariables.getGitSourceURLPassword())).call();
            if (!pullResult.isSuccessful()) {
                // TODO Handle conflict problems
                System.err.println("Pull Request Failed: " + pullResult.toString());
                return false;
            }
        }
        return true;

    }

    /**
     * @return
     */
    private boolean pushGitChanges() {
        try {
            boolean pullSuccess = pullGitChanges();
            if (!pullSuccess) {
                System.err.println("Problem pulling changes");
                return false;
            }
            git.add().addFilepattern(".").call();
            git.commit().setMessage("Changed from Loading UI").call();
            Iterable<PushResult> results = git.push().setCredentialsProvider(
                new UsernamePasswordCredentialsProvider(gitVariables.getGitSourceURLUserame(),
                    gitVariables.getGitSourceURLPassword())).call();

            for (PushResult pushResult : results) {
                java.util.Collection<RemoteRefUpdate> remoteUpdates = pushResult.getRemoteUpdates();
                for (RemoteRefUpdate ref : remoteUpdates) {
                    if (ref.getStatus() != RemoteRefUpdate.Status.OK) {
                        // TODO Handle conflict problems
                        System.err.println("Problem pushing changes");
                        return false;
                    }
                }
            }
            return true;
        } catch (GitAPIException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getDataLocalPath() {
        return gitVariables.getGitSourcePath() + gitVariables.getGitSourceDataSubpath();
    }

    public boolean validate(MultipartFile file) {

        // Validate content
        boolean valid = false;
        try {
            String content = IOUtils.toString(file.getInputStream(), StandardCharsets.UTF_8);

            if (Objects.requireNonNull(file.getContentType()).equals("text/xml")) {
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
            pullGitChanges();

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

                // Add itemId to collection
                if (itemMap.containsKey(itemName)) {
                    collection.getItemIds().add(itemMap.get(itemName).getId());
                } else {
                    String collectionDir = new File(collection.getFilepath()).getParentFile().getPath();
                    String itemPath = Paths.get(collectionDir).relativize(Paths.get(output.getCanonicalPath())).toString();
                    Id id = new Id(itemPath);
                    collection.getItemIds().add(id);
                }

                // Write out collection file
                ObjectMapper mapper = new ObjectMapper();
                ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
                writer.writeValue(new File(collection.getFilepath()), collection);

            }

            //TODO
            //pushGitChanges();

            updateModel();

            return true;

        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteItemFromCollection(String itemName, String collectionUrlSlug) {

        try {
            pullGitChanges();

            Collection collection = getCollection(collectionUrlSlug);
            Item item = itemMap.get(itemName);
            collection.getItemIds().remove(item.getId());

            // Write out collection file
            ObjectMapper mapper = new ObjectMapper();
            ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
            writer.writeValue(new File(collection.getFilepath()), collection);

            // remove file
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

            //TODO
            //pushGitChanges();

            updateModel();

            return true;
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateCollection(Collection collection) {
        try {

            // Write out file
            ObjectMapper mapper = new ObjectMapper();
            ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
            writer.writeValue(new File(collection.getFilepath()), collection);

            // Git Commit and push to remote repo.
            boolean success = pushGitChanges();
            updateModel();

            return success;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}

