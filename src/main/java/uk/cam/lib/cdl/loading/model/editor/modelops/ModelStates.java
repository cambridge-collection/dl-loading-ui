package uk.cam.lib.cdl.loading.model.editor.modelops;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.cam.lib.cdl.loading.model.editor.Collection;
import uk.cam.lib.cdl.loading.model.editor.Item;
import uk.cam.lib.cdl.loading.model.editor.ModelOps;

import java.nio.file.Path;

public class ModelStates {
    private ModelStates() {}

    public static ModelStateHandler<Item, Void> itemWriter(ModelOps modelOps, Path dataRoot) {
        return DefaultModelStateHandler.withoutResult(Item.class, ModelState.Ensure.PRESENT, state -> {
            modelOps.writeItem(dataRoot, state.model());
        });
    }

    public static ModelStateHandler<Item, Void> itemRemover(ModelOps modelOps, Path dataRoot) {
        return DefaultModelStateHandler.withoutResult(Item.class, ModelState.Ensure.ABSENT, state -> {
            modelOps.removeItem(dataRoot, state.model());
        });
    }

    public static ModelStateHandler<Collection, Void> collectionWriter(ModelOps modelOps, ObjectMapper mapper, Path dataRoot) {
        return DefaultModelStateHandler.withoutResult(Collection.class, ModelState.Ensure.PRESENT, state -> {
            modelOps.writeCollectionJson(mapper, dataRoot, state.model());
        });
    }

    public static ModelStateHandler<Collection, Void> collectionRemover(ModelOps modelOps, Path dataRoot) {
        return DefaultModelStateHandler.withoutResult(Collection.class, ModelState.Ensure.ABSENT, state -> {
            modelOps.removeCollection(dataRoot, state.model());
        });
    }
}
