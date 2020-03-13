package uk.cam.lib.cdl.loading.utils;

import uk.cam.lib.cdl.loading.dao.WorkspaceRepository;
import uk.cam.lib.cdl.loading.model.editor.Workspace;
import uk.cam.lib.cdl.loading.model.security.Role;

import java.util.ArrayList;
import java.util.List;

// TODO Read Roles from properties
public class RoleHelper {

    private final WorkspaceRepository repository;

    public RoleHelper(WorkspaceRepository repository) {
        this.repository = repository;
    }

    public List<Role> getAllRoles() {

        List<Role> allRoles = new ArrayList<>();
        for (Workspace workspace: repository.findAll()) {
            String workspaceMemberRole = WorkspaceRolesPrefix.WORKSPACE_MEMBER.stringValue + workspace.getId();
            String workspaceManagerRole = WorkspaceRolesPrefix.WORKSPACE_MANAGER.stringValue + workspace.getId();
            allRoles.add(new Role(workspaceMemberRole, "Member of Workspace: "+workspace.getName()));
            allRoles.add(new Role(workspaceManagerRole, "Admin of Workspace: "+workspace.getName()));
        }
        allRoles.add(new Role("ROLE_DEPLOYMENT_ALL_MANAGER","Deployment Manager"));
        allRoles.add(new Role("ROLE_SITE_MANAGER", "Site Manager"));

        return allRoles;
    }

    public String getWorkspaceMemberRole(Workspace workspace) {
        return WorkspaceRolesPrefix.WORKSPACE_MEMBER.stringValue +workspace.getId();
    }

    public String getWorkspaceManagerRole(Workspace workspace) {
        return WorkspaceRolesPrefix.WORKSPACE_MANAGER.stringValue +workspace.getId();
    }

    private enum WorkspaceRolesPrefix {
        WORKSPACE_MANAGER("ROLE_WORKSPACE_MANAGER"),
        WORKSPACE_MEMBER("ROLE_WORKSPACE_MEMBER");

        public final String stringValue;

        WorkspaceRolesPrefix(String stringValue) {
            this.stringValue = stringValue;
        }
    }

}
