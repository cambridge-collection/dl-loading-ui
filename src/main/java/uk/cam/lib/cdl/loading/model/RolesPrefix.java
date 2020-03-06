package uk.cam.lib.cdl.loading.model;

public enum RolesPrefix {
    SITE_MANAGER("ROLE_SITE_MANAGER"),
    WORKSPACE_MANAGER("ROLE_WORKSPACE_MANAGER"),
    WORKSPACE_MEMBER("ROLE_WORKSPACE_MEMBER"),
    DEPLOYMENT_MANAGER("ROLE_DEPLOYMENT_MANAGER");

    public final String role;

    RolesPrefix(String role) {
        this.role = role;
    }
}
