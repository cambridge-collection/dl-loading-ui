package uk.cam.lib.cdl.loading.model.editor;

import org.immutables.value.Value;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Value.Immutable(singleton = true)
@Value.Style(
    typeImmutable = "Default*",
    visibility = Value.Style.ImplementationVisibility.PACKAGE)
public interface ModelOps {
    static ModelOps ModelOps() {
        return DefaultModelOps.of();
    }

    default boolean collectionContainsItem(Collection collection, Item item) {
        return collectionContainsItem(collection, item.getId().getId());
    }
    default boolean collectionContainsItem(Collection collection, String itemId) {
        return collection.getResolvedItemIds().contains(itemId);
    }

    default String itemMetadataAsString(Item item) throws IOException {
        return Files.readString(item.getFilepath());
    }

    default List<String> resolvedItemIds(Collection collection) {

    }
}
