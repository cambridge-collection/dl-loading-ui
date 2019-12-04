package uk.cam.lib.cdl.loading.apis;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.util.FS;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.cam.lib.cdl.loading.config.EditConfig;
import uk.cam.lib.cdl.loading.config.GitConfig;
import uk.cam.lib.cdl.loading.config.GitLocalVariables;
import uk.cam.lib.cdl.loading.model.editor.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Uses a Bare Repository for testing jgit commands.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {EditConfig.class, GitConfig.class})
@SpringBootTest
class EditAPITest {

    private static final Logger LOG = LoggerFactory.getLogger(EditAPITest.class);

    private EditAPI editAPI;
    private GitLocalVariables gitSourceVariables;

    // Bare repo represents a mock version of the remote repo and it is cloned locally for testing.
    // content is added from the resources source-data dir.
    public EditAPITest() throws IOException, GitAPIException {

        // Create a folder in the temp folder that will act as the remote repository
        File remoteDir = File.createTempFile("remote", "");
        remoteDir.delete();
        remoteDir.mkdirs();

        // Create a bare repository
        RepositoryCache.FileKey fileKey = RepositoryCache.FileKey.exact(remoteDir, FS.DETECTED);
        Repository remoteRepo = fileKey.open(false);
        remoteRepo.create(true);

        // Clone the bare repository
        File cloneDir = File.createTempFile("clone", "");
        cloneDir.delete();
        cloneDir.mkdirs();

        Git git = Git.cloneRepository().setURI(remoteRepo.getDirectory().getAbsolutePath()).setDirectory(cloneDir).call();

        // Let's to our first commit
        // Create a new file
        File testSourceDir = new File("./src/test/resources/source-data");
        FileUtils.copyDirectory(testSourceDir, cloneDir);

        // Commit the new file
        git.add().addFilepattern(".").call();
        git.commit().setMessage("Adding Test Data").setAuthor("testuser", "test@example.com ").call();


        gitSourceVariables = new GitLocalVariables(cloneDir.getCanonicalPath(), "/data",
            "gitSourceURL", "gitSourceURLUserame",
            "gitSourceURLPassword", "gitBranch", "test.dl-dataset.json");


        GitHelper gitHelper = new GitHelper(git, gitSourceVariables);

        editAPI = new EditAPI(cloneDir.getCanonicalPath() + "/data",
            gitSourceVariables.getGitDatasetFilename(),
            gitSourceVariables.getGitSourcePath() + "/data/items/data/tei/", gitHelper);

    }

    @Test
    void updateModel() throws IOException, JSONException {

        // update a file directly on the file system
        String filePath = gitSourceVariables.getGitSourcePath() + "/data/collections/test.collection.json";
        String fileString = FileUtils.readFileToString(new File(filePath), "UTF-8");
        JSONObject testCollection = new JSONObject(fileString);
        String sortName = testCollection.getJSONObject("name").getString("sort");
        assert (sortName.equals("Sorting Test Name"));
        assert ("Sorting Test Name".equals(editAPI.getCollection("test").getName().getSort()));

        final JSONObject newName = testCollection.getJSONObject("name").put("sort", "New Value");
        final JSONObject newCollection = testCollection.put("name", newName);
        FileUtils.writeStringToFile(new File(filePath), newCollection.toString(), "UTF-8");

        editAPI.updateModel();

        // check model has been updated
        assert ("New Value".equals(editAPI.getCollection("test").getName().getSort()));
    }

    @Test
    void getCollections() {
        final List<Collection> collections = editAPI.getCollections();
        LOG.info("Collections: " + collections.size());
        assert (collections.size() == 1);
        assert (collections.get(0).getName().getUrlSlug().equals("test"));
        assert (collections.get(0).getItemIds().size() == 5);
    }

    @Test
    void getCollection() {
        Collection test = editAPI.getCollection("test");
        assert (test != null);
        assert (test.getName().getUrlSlug().equals("test"));
        assert (test.getItemIds().size() == 5);
    }

    @Test
    void getItem() {
        Item item = editAPI.getItem("MS-TEST-00001");
        assert (item != null);
        assert (item.getId().getId().equals("../items/data/tei/MS-TEST-00001/MS-TEST-00001.xml"));
        assert (item.getName().equals("MS-TEST-00001"));
        assert (item.getFilepath().endsWith("/data/items/data/tei/MS-TEST-00001/MS-TEST-00001.xml"));
    }

    @Test
    void validate() {
        MockMultipartFile jsonFile = new MockMultipartFile("json", "filename.json", "application/json", ("{\"json" +
            "\":\"someValue\"}").getBytes());
        MockMultipartFile xmlFile = new MockMultipartFile("xml", "filename.xml", "application/xml", ("<?xml " +
            "version=\"1.0\" encoding=\"UTF-8\"?><test></test>").getBytes());
        MockMultipartFile xmlFile2 = new MockMultipartFile("xml", "filename.xml", "text/xml", ("<?xml " +
            "version=\"1.0\" encoding=\"UTF-8\"?><test></test>").getBytes());

        boolean jsonValid = editAPI.validate(jsonFile);
        assert (!jsonValid);

        boolean xml1Valid = editAPI.validate(xmlFile);
        assert (xml1Valid);

        boolean xml2Valid = editAPI.validate(xmlFile2);
        assert (xml2Valid);

    }

    @Test
    void validateFilename() {
        boolean notValid1 = editAPI.validateFilename("THISITEMNAME");
        assert (!notValid1);
        boolean notValid2 = editAPI.validateFilename("THIS-ITEM-NAME");
        assert (!notValid2);
        boolean notValid3 = editAPI.validateFilename("THIS-ITEM-NAME-000000");
        assert (!notValid3);
        boolean notValid4 = editAPI.validateFilename("/THIS-ITEM-NAME-00000");
        assert (!notValid4);
        boolean valid = editAPI.validateFilename("THIS-ITEM-NAME-00000");
        assert (valid);
    }

    @Test
    void addItemToCollection() throws IOException {
        assert (editAPI.getItem("MS-MYITEMTEST-00001") == null);

        MockMultipartFile xmlFile2 = new MockMultipartFile("xml", "filename.xml", "text/xml", ("<?xml " +
            "version=\"1.0\" encoding=\"UTF-8\"?><test></test>").getBytes());

        editAPI.addItemToCollection("MS-MYITEMTEST-00001", "xml", xmlFile2.getInputStream(), "test");

        Item item = editAPI.getItem("MS-MYITEMTEST-00001");
        assert (item != null);
        assert (item.getId().getId().equals("../items/data/tei/MS-MYITEMTEST-00001/MS-MYITEMTEST-00001.xml"));
        assert (item.getName().equals("MS-MYITEMTEST-00001"));
        assert (item.getFilepath().endsWith("/data/items/data/tei/MS-MYITEMTEST-00001/MS-MYITEMTEST-00001.xml"));
        Id id = new Id("../items/data/tei/MS-MYITEMTEST-00001/MS-MYITEMTEST-00001.xml");
        assert (editAPI.getCollection("test").getItemIds().contains(id));
    }

    @Test
    void deleteItemFromCollection() {

        assert (editAPI.getCollection("test").getItemIds().contains(new Id("../items/data/tei/MS-TEST-00001/MS-TEST-00001.xml")));
        editAPI.deleteItemFromCollection("MS-TEST-00001", "test");
        assert (!editAPI.getCollection("test").getItemIds().contains(new Id("../items/data/tei/MS-TEST-00001/MS-TEST-00001.xml")));
    }

    @Test
    void updateCollection() {

        assert ("Sorting Test Name".equals(editAPI.getCollection("test").getName().getSort()));
        Collection collection = makeCollection("test");
        editAPI.updateCollection(collection);
        assert ("sortName".equals(editAPI.getCollection("test").getName().getSort()));
    }

/*    @Test
    void addCollection() {

        Collection collection = makeCollection("newCollection");
        assert (!editAPI.getCollections().contains(collection));
        editAPI.addCollection(collection);
        assert (editAPI.getCollections().contains(collection));
    }*/

    @Test
    void getDataLocalPath() {
        String dataLocalPath = editAPI.getDataLocalPath();
        LOG.info("dataLocalPath: " + dataLocalPath);
        assert (dataLocalPath.equals(gitSourceVariables.getGitSourcePath() + gitSourceVariables.getGitSourceDataSubpath()));
    }

    private Collection makeCollection(String urlSlugName) {
        CollectionName name = new CollectionName(urlSlugName, "sortName", "shortName", "fullName");
        CollectionDescription description = new CollectionDescription("shortDescription", new Id("fullDescription"),
            "mediumDescription");
        CollectionCredit credit = new CollectionCredit(new Id("proseCredit"));

        List<Id> itemIds = new ArrayList<>();
        itemIds.add(new Id("MS-TEST-00001"));

        Collection c = new Collection("collectionType", name, description, credit, itemIds);
        String filePath =
            gitSourceVariables.getGitSourcePath() + "/data/collections/" + urlSlugName + ".collection.json";
        c.setFilepath(filePath);
        c.setThumbnailURL("thumbnailURL");

        return c;
    }
}
