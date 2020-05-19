package uk.cam.lib.cdl.loading.model.editor;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Not used for Jackson serialisation
 */
public interface Item {
    Path id();
    Optional<String> fileData();
}
