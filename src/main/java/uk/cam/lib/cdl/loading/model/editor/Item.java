package uk.cam.lib.cdl.loading.model.editor;

/**
 * Not used for Jackson serialisation
 */
public class Item {

    private final String name;
    private final String filepath;
    private final Id id;

    public Item(String name, String filepath, Id id) {
        this.filepath = filepath;
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getFilepath() {
        return filepath;
    }

    public Id getId() {
        return id;
    }
}
