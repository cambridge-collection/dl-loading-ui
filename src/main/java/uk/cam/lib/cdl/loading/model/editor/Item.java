package uk.cam.lib.cdl.loading.model.editor;

import com.google.common.base.Preconditions;
import org.apache.commons.io.FilenameUtils;
import org.springframework.lang.Nullable;

import java.nio.file.Path;

import static uk.cam.lib.cdl.loading.model.editor.ModelOps.ModelOps;

/**
 * Not used for Jackson serialisation
 */
public class Item {
    private final Path id;

    public Item(Path id) {
        this.id = ModelOps().validatePathForId(id);
    }

    public Path id() {
        return this.id;
    }
}
