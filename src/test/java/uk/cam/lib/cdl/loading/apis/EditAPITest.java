package uk.cam.lib.cdl.loading.apis;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.truth.Truth;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import uk.cam.lib.cdl.loading.exceptions.EditApiException;
import uk.cam.lib.cdl.loading.exceptions.NotFoundException;
import uk.cam.lib.cdl.loading.model.editor.*;
import uk.cam.lib.cdl.loading.model.editor.modelops.ImmutableModelState;
import uk.cam.lib.cdl.loading.model.editor.ui.UICollection;
import uk.cam.lib.cdl.loading.testutils.Models;
import uk.cam.lib.cdl.loading.utils.sets.SetMembership;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.cam.lib.cdl.loading.model.editor.ModelOps.ModelOps;
import static uk.cam.lib.cdl.loading.model.editor.modelops.ModelState.Ensure.PRESENT;

/**
 * Uses a Bare Repository for testing jgit commands.
 */
@SpringBootTest
class EditAPITest {

    private static final Logger LOG = LoggerFactory.getLogger(EditAPITest.class);

    private static final Path ITEM_ID_MS_LATIN = Path.of("items/data/tei/MS-LATIN-00509/MS-LATIN-00509.xml");

    private static final Path COLLECTION_DIR = Path.of("collections");
    private static final Path COLLECTION_ID_TEST = COLLECTION_DIR.resolve("test.collection.json");
    private static final Path COLLECTION_ID_EMPTY1 = COLLECTION_DIR.resolve("empty1.collection.json");
    private static final Path COLLECTION_ID_EMPTY2 = COLLECTION_DIR.resolve("empty2.collection.json");

    private EditAPI editAPI;
    private File testSourceDir;

    // Bare repo represents a mock version of the remote repo and it is cloned locally for testing.
    // content is added from the resources source-data dir.
    public EditAPITest() throws IOException, EditApiException {


        Path tempDir = Files.createTempDirectory("EditAPITest");
        testSourceDir = tempDir.toFile();

        File testSourceDirOriginals = new File("./src/test/resources/source-data");
        FileUtils.copyDirectory(testSourceDirOriginals, testSourceDir);

        editAPI = new EditAPI(testSourceDir.getAbsolutePath() + "/data",
            "test.dl-dataset.json", "test.ui.json5",
            testSourceDir.getAbsolutePath() +"/data/items/data/tei/");

    }

    @Test
    void updateModel() throws IOException, JSONException, EditApiException {

        // update a file directly on the file system
        String filePath = testSourceDir.getAbsolutePath()+ "/data/collections/test.collection.json";
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
    public void getItemWithData_overloads() throws EditApiException {
        var itemWithoutData = editAPI.getItem(ITEM_ID_MS_LATIN);
        Truth.assertThat(editAPI.getItemWithData(itemWithoutData))
            .isEqualTo(editAPI.getItemWithData(ITEM_ID_MS_LATIN));
    }

    private void assertItemExistsWithContent(Item item) throws EditApiException, IOException {
        assertItemExistsOnDiskWithContent(item);
        assertItemExistsInEditApiWithContent(item);
    }

    private void assertItemExistsOnDiskWithContent(Item item) throws IOException {
        Preconditions.checkArgument(item.fileData().isPresent());
        assertThat(ModelOps().readMetadataAsString(editAPI.getDataLocalPath(), item.id())).isEqualTo(item.fileData().orElseThrow());
    }
    private void assertItemExistsInEditApiWithContent(Item item) throws EditApiException {
        Preconditions.checkArgument(item.fileData().isPresent());
        assertThat(editAPI.getItemWithData(item.id())).isEqualTo(item);
    }

    private void assertItemIsInCollections(Item item, Path...collectionIds)  {
        var expectedCollections = Stream.of(collectionIds).map(editAPI::getCollection).collect(toImmutableSet());
        var actualCollections = Models.collectionsContainingItem(item, editAPI.getCollections());
        assertThat(actualCollections).isEqualTo(expectedCollections);
    }

    private void assertItemDoesNotExist(Item item) {
        Preconditions.checkArgument(item.fileData().isEmpty());
        assertThrows(NoSuchFileException.class, () -> ModelOps().readMetadataAsString(editAPI.getDataLocalPath(), item.id()));
        assertThat(Models.collectionsContainingItem(item, editAPI.getCollections())).isEmpty();
    }

    @Test
    public void enforceItemState_modifyExistingItem() throws EditApiException, IOException {
        var item = editAPI.getItemWithData(ITEM_ID_MS_LATIN);
        var modifiedItem = ImmutableItem.copyOf(item).withFileData("foo");

        var enforced = editAPI.enforceItemState(modifiedItem, SetMembership.unchanged());

        Truth.assertThat(enforced).isEqualTo(Optional.of(ImmutableModelState.ensure(PRESENT, modifiedItem)));
        assertItemExistsWithContent(modifiedItem);
        assertItemIsInCollections(modifiedItem, COLLECTION_ID_TEST);
    }

    @Test
    public void enforceItemState_createItem() throws EditApiException, IOException {
        var content = "Example content";
        var id = Path.of("items/data/tei/MS-FOO-00001/MS-FOO-00001.xml");
        var newItem = ImmutableItem.of(id, content);

        assertItemDoesNotExist(newItem.withFileData(Optional.empty()));

        var enforced = editAPI.enforceItemState(newItem, SetMembership.onlyMemberOf(ImmutableSet.of(COLLECTION_ID_TEST)));

        assertThat(enforced).isEqualTo(Optional.of(ImmutableModelState.ensure(PRESENT, newItem)));
        assertItemExistsWithContent(newItem);
        assertItemIsInCollections(newItem, COLLECTION_ID_TEST);
    }

    @Test
    public void enforceItemState_changeCollections() throws EditApiException, IOException {
        var item = ImmutableItem.copyOf(editAPI.getItemWithData(ITEM_ID_MS_LATIN));
        assertItemExistsWithContent(item);
        assertItemIsInCollections(item, COLLECTION_ID_TEST);

        // Change the item from col1 to col2 and col3
        var enforced = editAPI.enforceItemState(
            // Don't change item
            item.withFileData(Optional.empty()),
            // Move item into empty1 and empty2
            SetMembership.onlyMemberOf(ImmutableSet.of(COLLECTION_ID_EMPTY1, COLLECTION_ID_EMPTY2)));

        assertThat(enforced).isEqualTo(Optional.empty());
        assertItemExistsWithContent(item);
        assertItemIsInCollections(item, COLLECTION_ID_EMPTY1, COLLECTION_ID_EMPTY2);
    }

    @Test
    public void enforceItemState_deleteItem() throws EditApiException, IOException {
        var itemWithContent = ImmutableItem.copyOf(editAPI.getItemWithData(ITEM_ID_MS_LATIN));
        var item = itemWithContent.withFileData(Optional.empty());
        assertItemExistsWithContent(itemWithContent);
        assertItemIsInCollections(itemWithContent, COLLECTION_ID_TEST);

        var enforced = editAPI.enforceItemState(item, SetMembership.onlyMemberOf(ImmutableSet.of()));

        assertThat(enforced).isEqualTo(Optional.of(ImmutableModelState.ensureAbsent(item)));
        assertItemDoesNotExist(item);
    }

    @Test
    public void enforceItemState_pathIdOverload() throws EditApiException {
        var item = editAPI.getItem(ITEM_ID_MS_LATIN);
        var enforced = editAPI.enforceItemState(item.id(), SetMembership.removing(COLLECTION_ID_TEST));
        Truth.assertThat(enforced).isEqualTo(Optional.of(ImmutableModelState.ensureAbsent(item)));
    }

    @Test
    void updateCollection() throws EditApiException {

        assert ("Sorting Test Name".equals(editAPI.getCollection("collections/test.collection.json").getName().getSort()));
        Collection collection = makeCollection("test");
        String descriptionHTML = "<html>test description</html>";
        String creditHTML = "<html>test credit</html>";
        editAPI.updateCollection(collection, descriptionHTML, creditHTML, makeUICollection(collection.getCollectionId()), ImmutableList.of((long)-1));
        assert ("sortName".equals(editAPI.getCollection("collections/test.collection.json").getName().getSort()));
    }

    @Test
    void addCollection() throws EditApiException {

        Collection collection = makeCollection("newCollection");
        assert (!editAPI.getCollections().contains(collection));
        String descriptionHTML = "<html>test description</html>";
        String creditHTML = "<html>test credit</html>";
        editAPI.updateCollection(collection, descriptionHTML, creditHTML, makeUICollection(collection.getCollectionId()), ImmutableList.of((long)-1));
        assert (editAPI.getCollections().contains(collection));
    }


    private Collection makeCollection(String urlSlugName) {
        String collectionId = "collections/" + urlSlugName + ".collection.json";
        CollectionName name = new CollectionName(urlSlugName, "sortName", "shortName", "fullName");
        CollectionDescription description = new CollectionDescription("shortDescription", new Id("fullDescription"),
            "mediumDescription");
        CollectionCredit credit = new CollectionCredit(new Id("proseCredit"));

        List<Id> itemIds = new ArrayList<>();
        itemIds.add(new Id("../items/data/tei/MS-TEST-00001/MS-TEST-00001.xml"));

        Collection c = new Collection(name, description, credit, itemIds, ImmutableList.of());
        String filePath =
            editAPI.getDataLocalPath()+ "/data/"+collectionId;
        c.setThumbnailURL("thumbnailURL");
        c.setCollectionId(collectionId);

        return c;
    }

    private UICollection makeUICollection(String collectionId) {
        return new UICollection(new Id(collectionId), "organisation", new Id("pages/images/collectionsView/collection-blank.jpg"));
    }
}
