package uk.cam.lib.cdl.loading.model.editor;

import java.io.IOException;
import java.nio.file.Files;

public class Model {
    private Model() {}

    public static boolean collectionContainsItem(Collection collection, Item item) {
        return collectionContainsItem(collection, item.getId().getId());
    }
    public static boolean collectionContainsItem(Collection collection, String itemId) {
        return collection.getResolvedItemIds().contains(itemId);
    }

    public static String itemMetadataAsString(Item item) throws IOException {
        return Files.readString(item.getFilepath());
    }
}
