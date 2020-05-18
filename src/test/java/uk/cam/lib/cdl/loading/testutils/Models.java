package uk.cam.lib.cdl.loading.testutils;

import com.google.common.collect.ImmutableList;
import uk.cam.lib.cdl.loading.model.editor.Collection;
import uk.cam.lib.cdl.loading.model.editor.CollectionCredit;
import uk.cam.lib.cdl.loading.model.editor.CollectionDescription;
import uk.cam.lib.cdl.loading.model.editor.CollectionName;
import uk.cam.lib.cdl.loading.model.editor.Id;

import java.nio.file.Path;
import java.util.stream.Stream;

public class Models {
    public static Collection exampleCollection(Path id) {
        return exampleCollection(id, Stream.of());
    }

    public static Collection exampleCollection(Path id, Stream<Id> itemIds) {
        var col = new Collection(
            new CollectionName("urlslug", "sort", "short", "full"),
            new CollectionDescription("short", new Id("description.html"), "medium"),
            new CollectionCredit(new Id("prose.html")),
            itemIds.collect(ImmutableList.toImmutableList()));

        col.setCollectionId(id.toString());
        return col;
    }
}
