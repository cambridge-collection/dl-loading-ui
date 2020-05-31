package uk.cam.lib.cdl.loading.model.editor;

import org.apache.commons.io.FilenameUtils;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Not used for Jackson serialisation
 */
public interface Item {
    Path id();
    Optional<String> fileData();

    /**
     * Get the Item's name.
     *
     * <p>The default implementation is to use the final portion of the {@link #id()}, without any file extension.</p>
     */
    default String name() {
        return FilenameUtils.removeExtension(id().getFileName().toString());
    }
}
