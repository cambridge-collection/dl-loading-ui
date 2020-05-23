package uk.cam.lib.cdl.loading.testutils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import uk.cam.lib.cdl.loading.model.editor.Collection;
import uk.cam.lib.cdl.loading.model.editor.CollectionCredit;
import uk.cam.lib.cdl.loading.model.editor.CollectionDescription;
import uk.cam.lib.cdl.loading.model.editor.CollectionName;
import uk.cam.lib.cdl.loading.model.editor.Id;
import uk.cam.lib.cdl.loading.model.editor.Item;
import uk.cam.lib.cdl.loading.model.editor.ModelOps;

import java.nio.file.Path;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static uk.cam.lib.cdl.loading.model.editor.ModelOps.ModelOps;

public class Models {
    public static Collection exampleCollection(Path id) {
        return exampleCollection(id, Stream.of());
    }
    public static Collection exampleCollection(Path id, String shortName) {
        return exampleCollection(id, shortName, Stream.of());
    }

    public static Collection exampleCollection(Path id, Stream<Id> itemIds) {
        return exampleCollection(id, "example", itemIds);
    }

    public static Collection exampleCollection(Path id, String shortName, Path...itemIds) {
        return exampleCollection(id, shortName, Stream.of(itemIds).map(idToReferenceFrom(id)));
    }

    public static Collection exampleCollection(Path id, String shortName, Stream<Id> itemIds) {
        Preconditions.checkNotNull(shortName);
        var col = new Collection(
            new CollectionName("urlslug", "sort", shortName, "full"),
            new CollectionDescription("short", new Id("description.html"), "medium"),
            new CollectionCredit(new Id("prose.html")),
            itemIds.collect(ImmutableList.toImmutableList()));

        col.setThumbnailURL("http://example.com/thumbnail.jpg");
        col.setCollectionId(id.toString());
        return col;
    }

    public static Function<Item, Id> itemToReferenceFrom(Path contextId) {
        return idToReferenceFrom(contextId).compose(Item::id);
    }
    public static Function<Path, Id> idToReferenceFrom(Path contextId) {
        return id -> new Id(ModelOps().relativizeIdAsReference(contextId, id));
    }
    public static Function<String, Id> idToReferenceFrom(String contextId) {
        return idToReferenceFrom(ModelOps().validatePathForId(Path.of(contextId))).compose(Path::of);
    }

    public static Stream<Collection> streamCollectionsContainingItem(Item item, Iterable<Collection> collections) {
        return Streams.stream(collections)
            .filter(col -> ModelOps().isItemInCollection(item, col));
    }
    public static Set<Collection> collectionsContainingItem(Item item, Iterable<Collection> collections) {
        return streamCollectionsContainingItem(item, collections)
            .collect(ImmutableSet.toImmutableSet());
    }
}
