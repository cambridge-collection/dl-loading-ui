package uk.cam.lib.cdl.loading.model.editor;

import com.google.common.base.Preconditions;

import java.nio.file.Path;

/**
 * Not used for Jackson serialisation
 */
public class Item {

    private final String name;
    private final Path filepath;
    private final Id id;

    public Item(String name, Path filepath, Id id) {
        Preconditions.checkArgument(filepath.isAbsolute(), "filepath is not absolute: %s", filepath);
        this.filepath = filepath.normalize();
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Path getFilepath() {
        return filepath;
    }

    public Id getId() {
        return id;
    }
}
