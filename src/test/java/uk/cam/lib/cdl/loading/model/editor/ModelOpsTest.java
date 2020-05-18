package uk.cam.lib.cdl.loading.model.editor;

import com.google.common.collect.ImmutableList;
import com.google.common.truth.Truth;
import org.immutables.value.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static uk.cam.lib.cdl.loading.model.editor.ModelOps.ModelOps;
import static uk.cam.lib.cdl.loading.testutils.Models.exampleCollection;

public class ModelOpsTest {

    private ModelOps modelOps = ModelOps();

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
        var item1 = new Item("foo1", Path.of("/data/items/foo1.xml"), new Id("items/foo1.xml"));
        var item2 = new Item("foo2", Path.of("/data/items/foo2.xml"), new Id("items/foo2.xml"));
        var item3 = new Item("foo3", Path.of("/data/items/foo3.xml"), new Id("items/foo3.xml"));

        var colId = Path.of("collections/example.json");

        return ImmutableCollectionItemsExample.builder()
            .addItems(item1, item2, item3)
            .collection(exampleCollection(colId,
                Stream.of(item1, item3).map(Item::getId).map(Id::getId).map(Path::of)
                    .map(itemId -> ModelOps().relativizeIdAsReference(colId, itemId)).map(Object::toString).map(Id::new)))
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

        Truth.assertThat(ModelOps().streamResolvedItemIds(col).collect(ImmutableList.toImmutableList()))
            .isEqualTo(ImmutableList.of(Path.of("items/foo1.xml"), Path.of("items/foo3.xml")));
    }
}
