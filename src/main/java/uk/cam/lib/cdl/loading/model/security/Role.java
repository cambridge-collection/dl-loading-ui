package uk.cam.lib.cdl.loading.model.security;

import java.util.Objects;

public class Role implements Comparable<Role>{

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

    @Override
    public int compareTo(Role role) {
        return name.compareTo(role.name);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Role) {
            Role r = (Role) object;
            return Objects.equals(getName(), r.getName()) &&
                Objects.equals(getDisplay(), r.getDisplay());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, display);
    }

}
