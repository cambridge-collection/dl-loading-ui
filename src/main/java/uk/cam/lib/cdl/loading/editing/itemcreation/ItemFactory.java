package uk.cam.lib.cdl.loading.editing.itemcreation;

import uk.cam.lib.cdl.loading.model.editor.Item;

import java.io.IOException;
import java.util.Set;

public interface ItemFactory {
    CreationResult<? extends Item> createItem(Set<ItemAttribute<?>> itemAttributes) throws IOException;
}
