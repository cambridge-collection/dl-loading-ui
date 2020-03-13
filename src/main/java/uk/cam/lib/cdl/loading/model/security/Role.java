package uk.cam.lib.cdl.loading.model.security;

public class Role {

    private final String name;
    private final String display;

    public Role(String name, String display) {
        this.name = name;
        this.display = display;
    }

    public String getName() {
        return name;
    }

    public String getDisplay() {
        return display;
    }
}
