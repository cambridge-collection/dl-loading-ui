package uk.cam.lib.cdl.loading.forms;

import org.immutables.value.Value;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;
import uk.cam.lib.cdl.loading.apis.EditAPI;
import uk.cam.lib.cdl.loading.model.editor.Collection;
import uk.cam.lib.cdl.loading.model.editor.Item;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.stream.Stream;

import static uk.cam.lib.cdl.loading.model.editor.ModelOps.ModelOps;

@org.immutables.value.Value.Modifiable
@org.immutables.value.Value.Style(
    create = "new", typeModifiable = "*", visibility = Value.Style.ImplementationVisibility.PUBLIC)
abstract class AbstractItemForm {
    @Nullable
    public abstract String metadata();
    @Nullable
    public abstract MultipartFile metadataFile();
    @Nullable
    public abstract MultipartFile paginationFile();
    @Value.Default
    public String[] collections() {
        return new String[]{};
    }

    public static ItemForm forItem(EditAPI editAPI, Item item) {
        var form = new ItemForm();
        try {
            form.setMetadata(ModelOps().readItemMetadataAsString(editAPI.getDataLocalPath(), item));
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        form.setCollections(editAPI.getCollections().stream()
            .filter(c -> ModelOps().isItemInCollection(item, c))
            .map(Collection::getCollectionId)
            .toArray(String[]::new));

        return form;
    }

    public boolean isCollectionSelected(Collection collection) {
        return Stream.of(collections()).anyMatch(c -> collection.getCollectionId().equals(c));
    }
}
