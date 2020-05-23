package uk.cam.lib.cdl.loading.apis;

import com.google.common.truth.Truth;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import uk.cam.lib.cdl.loading.config.GitLocalVariables;
import uk.cam.lib.cdl.loading.exceptions.EditApiException;
import uk.cam.lib.cdl.loading.exceptions.GitHelperException;
import uk.cam.lib.cdl.loading.exceptions.NotFoundException;
import uk.cam.lib.cdl.loading.model.editor.Collection;
import uk.cam.lib.cdl.loading.model.editor.CollectionCredit;
import uk.cam.lib.cdl.loading.model.editor.CollectionDescription;
import uk.cam.lib.cdl.loading.model.editor.CollectionName;
import uk.cam.lib.cdl.loading.model.editor.Id;
import uk.cam.lib.cdl.loading.model.editor.ImmutableItem;
import uk.cam.lib.cdl.loading.model.editor.Item;
import uk.cam.lib.cdl.loading.model.editor.modelops.ImmutableModelState;
import uk.cam.lib.cdl.loading.model.editor.modelops.ModelState;
import uk.cam.lib.cdl.loading.utils.GitHelper;
import uk.cam.lib.cdl.loading.utils.sets.SetMembership;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.cam.lib.cdl.loading.model.editor.ModelOps.ModelOps;

/**
 * Uses a Bare Repository for testing jgit commands.
 */
@SpringBootTest
class EditAPITest {

    private static final Logger LOG = LoggerFactory.getLogger(EditAPITest.class);

    private static final Path ITEM_ID_MS_LATIN = Path.of("items/data/tei/MS-LATIN-00509/MS-LATIN-00509.xml");

    private EditAPI editAPI;
    private GitLocalVariables gitSourceVariables;

    // Bare repo represents a mock version of the remote repo and it is cloned locally for testing.
    // content is added from the resources source-data dir.
    public EditAPITest() throws IOException, GitAPIException, GitHelperException, EditApiException {

        MockGitRepo gitRepo = new MockGitRepo();
        Git git = gitRepo.getGit();

        // Let's do our first commit
        // Create a new file
        File testSourceDir = new File("./src/test/resources/source-data");
        FileUtils.copyDirectory(testSourceDir, gitRepo.getCloneDir());

        // Commit the new file
        git.add().addFilepattern(".").call();
        git.commit().setMessage("Adding Test Data").setAuthor("testuser", "test@example.com ").call();


        gitSourceVariables = new GitLocalVariables(gitRepo.getCloneDir().getCanonicalPath(), "data",
            "gitSourceURL", "gitSourceURLUserame",
            "gitSourceURLPassword", "gitBranch");


        GitHelper gitHelper = new GitHelper(git, gitSourceVariables);

        editAPI = new EditAPI(gitRepo.getCloneDir().getCanonicalPath() + "/data",
            "test.dl-dataset.json", "test.ui.json5",
            gitSourceVariables.getGitSourcePath() + "/data/items/data/tei/", gitHelper);

    }

    @Test
    void updateModel() throws IOException, JSONException, EditApiException {

        // update a file directly on the file system
        String filePath = gitSourceVariables.getGitSourcePath() + "/data/collections/test.collection.json";
        String fileString = FileUtils.readFileToString(new File(filePath), "UTF-8");
        JSONObject testCollection = new JSONObject(fileString);
        String sortName = testCollection.getJSONObject("name").getString("sort");
        assert (sortName.equals("Sorting Test Name"));
        assert ("Sorting Test Name".equals(editAPI.getCollection("collections/test.collection.json").getName().getSort()));

        final JSONObject newName = testCollection.getJSONObject("name").put("sort", "New Value");
        final JSONObject newCollection = testCollection.put("name", newName);
        FileUtils.writeStringToFile(new File(filePath), newCollection.toString(), "UTF-8");

        editAPI.updateModel();

        // check model has been updated
        assert ("New Value".equals(editAPI.getCollection("collections/test.collection.json").getName().getSort()));
    }

    @Test
    void getCollections() {
        var collections = editAPI.getCollections();
        assertThat(collections).hasSize(3);
        assertThat(collections).hasSize(3);
        assertThat(collections.get(0).getName().getUrlSlug()).isEqualTo("test");
        assertThat(collections.get(0).getItemIds()).hasSize(5);

        // copies are returned
        var collectionsB = editAPI.getCollections();
        assertThat(collections).isEqualTo(collectionsB);
        assertThat(collections.get(0)).isNotSameInstanceAs(collectionsB.get(0));
    }

    @Test
    void getCollection() {
        var id = Path.of("collections/test.collection.json");
        Collection a = editAPI.getCollection(id.toString());
        Collection b = editAPI.getCollection(id.toString());
        Collection c = editAPI.getCollection(id);
        assertThat(a).isEqualTo(b);
        assertThat(a).isEqualTo(c);
        // copies are returned
        assertThat(a).isNotSameInstanceAs(b);
        assertThat(a).isNotSameInstanceAs(c);
        assertThat(b).isNotSameInstanceAs(c);

        assertThat(a.getName().getUrlSlug().equals("test"));
        assertThat(a.getItemIds()).hasSize(5);
    }

    @Test
    void getCollectionThrowsOnUnknownId() {
        assertThrows(NotFoundException.class, () -> editAPI.getCollection(Path.of("sdfsd")));
        assertThrows(NotFoundException.class, () -> editAPI.getCollection(("sdfsd")));
    }

    @Test
    void getItem() {
        var id = Path.of("items/data/tei/MS-TEST-00001/MS-TEST-00001.xml");
        Item item = editAPI.getItem(id);
        Item itemFromStringId = editAPI.getItem(id.toString());
        assertThat(item).isSameInstanceAs(itemFromStringId);
        assertThat((Object)item.id()).isEqualTo(id);
    }

    @Test
    void getItemThrowsOnUnknownId() {
        assertThrows(NotFoundException.class, () -> editAPI.getItem(Path.of("sdfsd")));
        assertThrows(NotFoundException.class, () -> editAPI.getItem(("sdfsd")));
    }

    @Test
    void validate() throws IOException {
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
    public void itemExists() {
        Truth.assertThat(editAPI.itemExists(ITEM_ID_MS_LATIN)).isTrue();
        Truth.assertThat(editAPI.itemExists(Path.of("items/missing"))).isFalse();
    }

    @Test
    public void itemWithData() throws EditApiException, IOException {
        var item = editAPI.getItemWithData(ITEM_ID_MS_LATIN);
        Truth.assertThat(item.fileData().orElseThrow())
            .isEqualTo(ModelOps().readMetadataAsString(editAPI.getDataLocalPath(), item.id()));
    }

    @Test
    public void enforceItemState_modifyExistingItem() throws EditApiException, IOException {
        var item = editAPI.getItemWithData(ITEM_ID_MS_LATIN);
        var modifiedItem = ImmutableItem.copyOf(item).withFileData("foo");

        var enforced = editAPI.enforceItemState(modifiedItem, SetMembership.unchanged());

        Truth.assertThat(enforced).isEqualTo(Optional.of(ImmutableModelState.ensure(ModelState.Ensure.PRESENT, modifiedItem)));
        Truth.assertThat(editAPI.getItemWithData(ITEM_ID_MS_LATIN)).isEqualTo(modifiedItem);
        // The change is really on disk...
        Truth.assertThat(ModelOps().readMetadataAsString(editAPI.getDataLocalPath(), ITEM_ID_MS_LATIN))
            .isEqualTo("foo");
    }

    @Test
    void addItemToCollection() throws IOException, EditApiException {
        var itemId = Path.of("items/data/tei/MS-MYITEMTEST-00001/MS-MYITEMTEST-00001.xml");
        assertThrows(NotFoundException.class, () -> editAPI.getItem(itemId));

        MockMultipartFile xmlFile2 = new MockMultipartFile("xml", "filename.xml", "text/xml", ("<?xml " +
            "version=\"1.0\" encoding=\"UTF-8\"?><test></test>").getBytes());

        editAPI.addItemToCollection("MS-MYITEMTEST-00001", "xml", xmlFile2.getInputStream(), "collections/test.collection.json");

        Item item = editAPI.getItem(itemId);
        assertThat((Object)item.id()).isEqualTo(itemId);
        Id id = new Id("../items/data/tei/MS-MYITEMTEST-00001/MS-MYITEMTEST-00001.xml");
        assert (editAPI.getCollection("collections/test.collection.json").getItemIds().contains(id));
    }

    @Test
    void deleteItemFromCollection() throws EditApiException {
        var itemId = Path.of("items/data/tei/MS-TEST-00001/MS-TEST-00001.xml");
        var itemFile = ModelOps().resolveIdToIOPath(editAPI.getDataLocalPath(), itemId);
        var collectionId = Path.of("collections/test.collection.json");
        var itemReference = new Id("../items/data/tei/MS-TEST-00001/MS-TEST-00001.xml");

        assertThat(editAPI.getCollection(collectionId).getItemIds()).contains(itemReference);
        assertThat(Files.isRegularFile(itemFile));

        editAPI.deleteItemFromCollection(itemId, Path.of("collections/test.collection.json"));

        assertThat(editAPI.getCollection(collectionId).getItemIds()).doesNotContain(itemReference);
        assertThat(Files.notExists(itemFile)).isTrue();
    }

    @Test
    void updateCollection() throws EditApiException {

        assert ("Sorting Test Name".equals(editAPI.getCollection("collections/test.collection.json").getName().getSort()));
        Collection collection = makeCollection("test");
        String descriptionHTML = "<html>test description</html>";
        String creditHTML = "<html>test credit</html>";
        editAPI.updateCollection(collection, descriptionHTML, creditHTML);
        assert ("sortName".equals(editAPI.getCollection("collections/test.collection.json").getName().getSort()));
    }

    @Test
    void addCollection() throws EditApiException {

        Collection collection = makeCollection("newCollection");
        assert (!editAPI.getCollections().contains(collection));
        String descriptionHTML = "<html>test description</html>";
        String creditHTML = "<html>test credit</html>";
        editAPI.updateCollection(collection, descriptionHTML, creditHTML);
        assert (editAPI.getCollections().contains(collection));
    }

    @Test
    void getDataLocalPath() {
        var dataLocalPath = editAPI.getDataLocalPath();
        LOG.info("dataLocalPath: " + dataLocalPath);
        assertThat((Object)dataLocalPath)
            .isEqualTo(Path.of(gitSourceVariables.getGitSourcePath(), gitSourceVariables.getGitSourceDataSubpath()));
    }

    private Collection makeCollection(String urlSlugName) {
        String collectionId = "collections/" + urlSlugName + ".collection.json";
        CollectionName name = new CollectionName(urlSlugName, "sortName", "shortName", "fullName");
        CollectionDescription description = new CollectionDescription("shortDescription", new Id("fullDescription"),
            "mediumDescription");
        CollectionCredit credit = new CollectionCredit(new Id("proseCredit"));

        List<Id> itemIds = new ArrayList<>();
        itemIds.add(new Id("../items/data/tei/MS-TEST-00001/MS-TEST-00001.xml"));

        Collection c = new Collection(name, description, credit, itemIds);
        String filePath =
            gitSourceVariables.getGitSourcePath() + "/data/"+collectionId;
        c.setThumbnailURL("thumbnailURL");
        c.setCollectionId(collectionId);

        return c;
    }
}
