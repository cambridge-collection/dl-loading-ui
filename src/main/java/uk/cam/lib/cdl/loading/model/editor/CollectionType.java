package uk.cam.lib.cdl.loading.model.editor;

public enum CollectionType {
    ORGANISATION ("organisation"),
    VIRTUAL ("virtual"),
    PARENT ("parent");

    private final String name;

    private CollectionType(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return name.equals(otherName);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
