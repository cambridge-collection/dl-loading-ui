package uk.cam.lib.cdl.loading.editing.itemcreation;

import uk.cam.lib.cdl.loading.model.editor.Item;

import java.util.Set;

public interface ItemFactory {
    CreationResult<Item> createItem(Set<ItemAttribute<?>> itemAttributes);

    //    interface ItemDetails {
//        String filename();
//        String fileData();
//    }

}
