package uk.cam.lib.cdl.loading.model.editor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharSource;
import com.google.common.truth.Truth;
import org.immutables.value.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.cam.lib.cdl.loading.model.editor.ModelOps.ModelOps;
import static uk.cam.lib.cdl.loading.testutils.Models.*;

public class ModelOpsTest {
    @Test
    public void instanceAccessFunction() {
        Truth.assertThat(ModelOps()).isInstanceOf(ModelOps.class);
    }

    @ParameterizedTest
    @CsvSource({
        "'',true",
        "/foo/bar,true",
        "foo/bar,true",
        "./foo/bar,false",
        "foo/./bar,false",
        "foo/../bar,false",
        "../foo/bar,false",
    })
    public void pathIsNormalised(Path path, boolean isNormalised) {
        Truth.assertThat(ModelOps().pathIsNormalised(path)).isEqualTo(isNormalised);
    }

    @ParameterizedTest
    @CsvSource({
        "'',false",
        "'/foo',false",
        "'foo/../bar.txt',false",
        "'foo',true",
        "'foo/bar.txt',true"
    })
    public void validatePathForId(Path path, boolean isValid) {
        if(isValid) {
            Truth.assertThat((Object)ModelOps().validatePathForId(path)).isSameInstanceAs(path);
        }
        else {
            Assertions.assertThrows(IllegalStateException.class, () -> ModelOps().validatePathForId(path));
        }
    }

    @ParameterizedTest
    @CsvSource({
        "'',false",
        "'/foo',false",
        "'foo/../bar',false",
        "'foo',true",
        "'foo/bar',true",
        // paths can have extensions, but the intent is that this is used for directory references
        "'foo/bar.txt',true"
    })
    public void validateSubpath(Path path, boolean isValid) {
        if(isValid) {
            Truth.assertThat((Object)ModelOps().validateSubpath(path)).isSameInstanceAs(path);
        }
        else {
            Assertions.assertThrows(IllegalStateException.class, () -> ModelOps().validateSubpath(path));
        }
    }

    @ParameterizedTest
    @CsvSource({
        "/,foo,/foo,true",
        "/foo,bar,/foo/bar,true",
        "/foo/bar,baz/boz,/foo/bar/baz/boz",
        // Invalid:
        "foo,bar,",
        "/foo/../bar,baz,",
        "/foo,/bar,",
        "/foo,bar/../baz,"
    })
    public void resolveIdToIOPath(Path root, Path id, @Nullable Path expected) {
        if(expected != null) {
            Truth.assertThat((Object)ModelOps().resolveIdToIOPath(root, id)).isEqualTo(expected);
        }
        else {
            Assertions.assertThrows(IllegalStateException.class, () -> ModelOps().resolveIdToIOPath(root, id));
        }
    }

    @ParameterizedTest
    @CsvSource({
        "collections/foo.json,../items/item.json,items/item.json",
        "root/collections/foo.json,../items/item.json,root/items/item.json",
    })
    public void resolveReferenceAsId(Path contextFileId, Path reference, Path expected) {
        Truth.assertThat((Object)ModelOps().resolveReferenceAsId(contextFileId, reference)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        // context is not a valid id
        "/collections/foo.json,../items/item.json",
        // reference outside implied root
        "collections/foo.json,../../items/item.json"
    })
    public void resolveReferenceAsIdRejectsInvalid(Path contextFileId, Path reference) {
        Assertions.assertThrows(IllegalStateException.class, () -> ModelOps().resolveReferenceAsId(contextFileId, reference));
    }

    @ParameterizedTest
    @CsvSource({
        "collections/foo.json,items/item.json,../items/item.json",
        "root/collections/foo.json,root/items/item.json,../items/item.json",
    })
    public void relativizeIdAsReference(Path contextFileId, Path id, Path expected) {
        Truth.assertThat((Object)ModelOps().relativizeIdAsReference(contextFileId, id)).isEqualTo(expected);
    }

    @Value.Immutable
    interface CollectionItemsExample {
        Collection collection();
        List<Item> items();
    }

    public static CollectionItemsExample example1() {
        var item1 = ImmutableItem.of(Path.of("items/foo1.xml"));
        var item2 = ImmutableItem.of(Path.of("items/foo2.xml"));
        var item3 = ImmutableItem.of(Path.of("items/foo3.xml"));

        var colId = Path.of("collections/example.json");

        return ImmutableCollectionItemsExample.builder()
            .addItems(item1, item2, item3)
            .collection(exampleCollection(colId,
                Stream.of(item1, item3)
                    .map(itemToReferenceFrom(colId))))
            .build();
    }

    @Test
    public void collectionContainsItem() {
        var example1 = example1();
        var item1 = example1.items().get(0);
        var item2 = example1.items().get(1);
        var item3 = example1.items().get(2);

        var col = example1.collection();

        Truth.assertThat(ModelOps().isItemInCollection(item1, col)).isTrue();
        Truth.assertThat(ModelOps().isItemInCollection(item2, col)).isFalse();
        Truth.assertThat(ModelOps().isItemInCollection(item3, col)).isTrue();
        Truth.assertThat(ModelOps().isItemInCollection(Path.of("items/foo1.xml"), col)).isTrue();
        Truth.assertThat(ModelOps().isItemInCollection(Path.of("items/blah.xml"), col)).isFalse();
    }

    @Test
    public void streamResolvedItemIds() {
        var example1 = example1();

        var col = example1.collection();

        Truth.assertThat(ModelOps().streamResolvedItemIds(col).collect(toImmutableList()))
            .isEqualTo(ImmutableList.of(Path.of("items/foo1.xml"), Path.of("items/foo3.xml")));
    }

    @Test
    public void addItemToCollection() {
        var colId = Path.of("collections/foo.json");
        var itemId = Path.of("items/item.json");
        var col = exampleCollection(colId);
        var item = ImmutableItem.of(itemId);

        Truth.assertThat(col.getItemIds()).isEmpty();

        // When
        Truth.assertThat(ModelOps().addItemToCollection(col, item)).isTrue();
        // Then
        Truth.assertThat(col.getItemIds()).hasSize(1);
        Truth.assertThat(ModelOps().isItemInCollection(item, col)).isTrue();

        // Item is not added twice
        Truth.assertThat(ModelOps().addItemToCollection(col, item)).isFalse();
        Truth.assertThat(col.getItemIds()).hasSize(1);
    }

    @Test
    public void removeItemFromCollection() {
        var colId = Path.of("collections/foo.json");
        var itemId = Path.of("items/item-2.json");
        // Collection with 3 items
        var col = exampleCollection(colId, Stream.of(1, 2, 3)
            .map(n -> "items/item-" + n + ".json").map(Path::of)
            .map(idToReferenceFrom(colId)));
        var item = ImmutableItem.of(itemId);

        Truth.assertThat(col.getItemIds()).hasSize(3);
        Truth.assertThat(ModelOps().isItemInCollection(item, col)).isTrue();

        Truth.assertThat(ModelOps().removeItemFromCollection(col, item)).isTrue();

        Truth.assertThat(col.getItemIds()).hasSize(2);
        Truth.assertThat(ModelOps().isItemInCollection(item, col)).isFalse();

        Truth.assertThat(ModelOps().removeItemFromCollection(col, item)).isFalse();
    }

    @Test
    public void writeCollectionJson(@TempDir Path dataDir) throws IOException {
        Truth.assertThat(Files.list(dataDir).count()).isEqualTo(0);

        var colId = Path.of("collections/foo.json");

        for(var mode : List.of("new", "overwrite")) {
            var col = exampleCollection(colId, "col-" + mode);
            ModelOps().writeCollectionJson(new ObjectMapper(), dataDir, col);

            Truth.assertThat(Files.isDirectory(dataDir.resolve("collections"))).isTrue();
            Truth.assertThat(new ObjectMapper().readValue(
                ModelOps().resolveIdToIOPath(dataDir, colId).toFile(), Collection.class))
                .isEqualTo(col);
        }
    }

    @Test
    public void writeItem(@TempDir Path dataDir) throws IOException {
        var item = ImmutableItem.of(Path.of("items/foo/foo.txt"));
        var itemFile = ModelOps().resolveIdToIOPath(dataDir, item.id());
        boolean fileExists = false;
        for(var content : List.of("foo\nß∂ƒ\n", "bar\n∆˚¬\n")) {
            Truth.assertThat(Files.exists(itemFile)).isEqualTo(fileExists);
            ModelOps().writeItem(dataDir, item.withFileData(content));
            Truth.assertThat(Files.readString(itemFile)).isEqualTo(content);
            fileExists = true;
        }
    }

    @Test
    public void writeItemRejectsItemWithoutFileData(@TempDir Path dataDir) {
        var exc = Assertions.assertThrows(IllegalArgumentException.class,
            () -> ModelOps().writeItem(dataDir, ImmutableItem.of(Path.of("items/foo/foo.txt"))));
        Truth.assertThat(exc).hasMessageThat().startsWith("item has no file data");
    }

    @Test
    public void writeMetadata_String(@TempDir Path dataDir) throws IOException {
        testWriteMetadata(dataDir, ModelOps()::writeMetadata);
    }

    @Test
    public void writeMetadata_InputStream(@TempDir Path dataDir) throws IOException {
        testWriteMetadata(dataDir, (_dataDir, id, content) -> ModelOps().writeMetadata(_dataDir, id,
            CharSource.wrap(content)
                .asByteSource(Charsets.UTF_8)
                .openBufferedStream()));
    }

    @FunctionalInterface interface WriteMetadataOverload { void callViaString(Path dir, Path id, String content) throws IOException; }

    void testWriteMetadata(Path dataDir, WriteMetadataOverload writeMetadataOverload) throws IOException {
        var id = Path.of("some/dir/file.txt");
        var content = "some\ncontent\n∂ƒß¬˚∆ß∂åƒ\n";
        var ioPath = ModelOps().resolveIdToIOPath(dataDir, id);

        Truth.assertThat(Files.isDirectory(ioPath.getParent())).isFalse();
        Truth.assertThat(Files.exists(ioPath)).isFalse();

        for(var mode : List.of("new", "overwrite")) {
            writeMetadataOverload.callViaString(dataDir, id, content + mode);

            Truth.assertWithMessage(mode).that(Files.isDirectory(ioPath.getParent())).isTrue();
            Truth.assertWithMessage(mode).that(Files.readString(ioPath)).isEqualTo(content + mode);
        }
    }

    @Test
    public void removeMetadataRemovesFileAndParentDirectories(@TempDir Path dataDir) throws IOException {
        Path id = Path.of("some/dir/foo.txt");
        Path file = ModelOps().resolveIdToIOPath(dataDir, id);
        Files.createDirectories(file.getParent());
        Files.writeString(file, "content");
        Truth.assertThat(Files.readString(file)).isEqualTo("content");

        Truth.assertThat(ModelOps().removeMetadata(dataDir, id)).isTrue();
        // dir and contents are gone
        Truth.assertThat(Files.exists(dataDir.resolve("some"))).isFalse();
        Truth.assertThat(Files.exists(dataDir)).isTrue();

        // Removing a missing file succeeds but results false
        Truth.assertThat(ModelOps().removeMetadata(dataDir, id)).isFalse();
    }

    @Test
    public void removeMetadataDoesntRemoveDirectoriesWhenFileDoesNotExist(@TempDir Path dataDir) throws IOException {
        Path id = Path.of("some/dir/foo.txt");
        Path file = ModelOps().resolveIdToIOPath(dataDir, id);
        Files.createDirectories(file.getParent());

        Truth.assertThat(ModelOps().removeMetadata(dataDir, id)).isFalse();
        Truth.assertThat(Files.exists(dataDir.resolve("some/dir"))).isTrue();
    }

    @Test
    public void cleanEmptyDirectories(@TempDir Path dataDir) throws IOException {
        Path rel = Path.of("some/dir/a/b/removed-file.txt");
        Path abs = ModelOps().resolveIdToIOPath(dataDir, rel);
        Files.createDirectories(abs.getParent());
        Truth.assertThat(Files.isDirectory(abs.getParent())).isTrue();

        ModelOps().cleanEmptyDirectories(dataDir, rel);

        Truth.assertThat(Files.isDirectory(dataDir.resolve("some"))).isFalse();
        Truth.assertThat(Files.isDirectory(dataDir)).isTrue();
    }

    @Test
    public void cleanEmptyDirectoriesPreservesNonEmptyDirs(@TempDir Path dataDir) throws IOException {
        Path id = Path.of("some/dir/a/b/removed-file.txt");
        Path other = Path.of("some/dir/x");
        Path absId = dataDir.resolve(id);
        Path absOther = dataDir.resolve(other);
        Files.createDirectories(absId.getParent());
        Files.createDirectories(absOther);
        Truth.assertThat(Files.isDirectory(absId.getParent())).isTrue();
        Truth.assertThat(Files.isDirectory(absOther)).isTrue();

        ModelOps().cleanEmptyDirectories(dataDir, id);

        Truth.assertThat(Files.isDirectory(dataDir.resolve("some/dir"))).isTrue();
        Truth.assertThat(Files.isDirectory(dataDir.resolve("some/dir/a"))).isFalse();
        Truth.assertThat(Files.isDirectory(absOther)).isTrue();
    }

    @Test
    public void removeItem() throws IOException {
        var mockModelOps = mock(ModelOps.class);
        when(mockModelOps.removeItem(any(), any())).thenCallRealMethod();
        when(mockModelOps.removeMetadata(any(), any())).thenReturn(true);
        var dataRoot = Path.of("/example/root");
        var id = Path.of("items/item.json");
        var item = ImmutableItem.of(id);

        Truth.assertThat(mockModelOps.removeItem(dataRoot, item)).isTrue();

        verify(mockModelOps, times(1)).removeItem(dataRoot, item);
        verify(mockModelOps, times(1)).removeMetadata(dataRoot, id);
        verifyNoMoreInteractions(mockModelOps);
    }

    @Test
    public void removeCollection() throws IOException {
        var mockModelOps = mock(ModelOps.class);
        when(mockModelOps.removeCollection(any(), any())).thenCallRealMethod();
        when(mockModelOps.removeMetadata(any(), any())).thenReturn(true);
        var dataRoot = Path.of("/example/root");
        var id = Path.of("collections/col.json");
        var collection = exampleCollection(id);

        Truth.assertThat(mockModelOps.removeCollection(dataRoot, collection)).isTrue();

        verify(mockModelOps, times(1)).removeCollection(dataRoot, collection);
        verify(mockModelOps, times(1)).removeMetadata(dataRoot, id);
        verifyNoMoreInteractions(mockModelOps);
    }

    @Test
    public void readMetadataAsString(@TempDir Path dataDir) throws IOException {
        var id = Path.of("some/dir/file.txt");
        var content = "some\ncontent\n∂ƒß¬˚∆ß∂åƒ\n";
        var ioPath = ModelOps().resolveIdToIOPath(dataDir, id);

        Files.createDirectories(ioPath.getParent());
        Files.writeString(ioPath, content);

        Truth.assertThat(ModelOps().readMetadataAsString(dataDir, id)).isEqualTo(content);
    }

    @Test
    public void readItemMetadataAsString(@TempDir Path dataDir) throws IOException {
        var id = Path.of("some/dir/file.txt");
        var content = "some\ncontent\n∂ƒß¬˚∆ß∂åƒ\n";
        var ioPath = ModelOps().resolveIdToIOPath(dataDir, id);

        Files.createDirectories(ioPath.getParent());
        Files.writeString(ioPath, content);

        Truth.assertThat(ModelOps().readItemMetadataAsString(dataDir, ImmutableItem.of(id))).isEqualTo(content);
    }
}
