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
    @Nullable
    private final Path absolutePath;

    public Item(Path id) {
        this.id = ModelOps().validatePathForId(id);
        this.absolutePath = null;
    }

    /**
     * Deprecated in favor of holding only the item ID in the model. The
     * absolute path is not part of the Item's state, rather is defined by the
     * data directory it was loaded from.
     */
    @Deprecated
    public Item(String name, Path filepath, Id id) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(filepath);
        Preconditions.checkNotNull(id);

        this.id = ModelOps().validatePathForId(Path.of(id.getId()));
        this.absolutePath = ModelOps().validatePathForIO(filepath);
    }

    public Path getIdAsPath() {
        return this.id;
    }

    @Deprecated
    public String getName() {
        return FilenameUtils.getBaseName(id.getFileName().toString());
    }

    @Deprecated
    public Path getFilepath() {
        if(absolutePath == null) {
            throw new UnsupportedOperationException("getFilepath() called on Item constructed with ID-only constructor");
        }
        return absolutePath;
    }

    @Deprecated
    public Id getId() {
        return new Id(this.id.toString());
    }
}
