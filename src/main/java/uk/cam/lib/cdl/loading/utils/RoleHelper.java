package uk.cam.lib.cdl.loading.utils;

import org.springframework.security.core.Authentication;
import uk.cam.lib.cdl.loading.dao.WorkspaceRepository;
import uk.cam.lib.cdl.loading.model.editor.Workspace;
import uk.cam.lib.cdl.loading.model.security.Role;
import uk.cam.lib.cdl.loading.security.RoleService;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RoleHelper {

    private final WorkspaceRepository repository;
    private static final String ROLE_WORKSPACE_MANAGER = "ROLE_WORKSPACE_MANAGER";
    private static final String ROLE_WORKSPACE_MEMBER = "ROLE_WORKSPACE_MEMBER";
    private static final String ROLE_DEPLOYMENT_MANAGER = "ROLE_DEPLOYMENT_MANAGER";
    private static final String ROLE_SITE_MANAGER = "ROLE_SITE_MANAGER";

    public RoleHelper(WorkspaceRepository repository) {
        this.repository = repository;
    }

    public List<Role> getAllRoles() {

        List<Role> allRoles = new ArrayList<>();
        for (Workspace workspace : repository.findAll()) {
            allRoles.add(new Role(ROLE_WORKSPACE_MEMBER + workspace.getId(), "Member of Workspace: " + workspace.getName()));
            allRoles.add(new Role(ROLE_WORKSPACE_MANAGER + workspace.getId(), "Admin of Workspace: " + workspace.getName()));
        }
        allRoles.add(new Role(ROLE_DEPLOYMENT_MANAGER, "Deployment Manager"));
        allRoles.add(new Role(ROLE_SITE_MANAGER, "Site Manager"));

        return allRoles;
    }

    public List<Role> getRolesUserCanAssign(Authentication authentication) {

        List<Role> roles = new ArrayList<>();
        List<Role> allRoles = getAllRoles();
        RoleService roleService = new RoleService();

        for (Role role : allRoles) {

            switch (role.getName()) {
                case ROLE_SITE_MANAGER:
                    if (roleService.canAssignRoleSiteManager(authentication)) {
                        roles.add(role);
                    }
                    break;
                case ROLE_DEPLOYMENT_MANAGER:
                    if (roleService.canAssignRoleDeploymentManager(authentication)) {
                        roles.add(role);
                    }
                    break;
            }

            if (role.getName().startsWith(ROLE_WORKSPACE_MANAGER)) {
                Pattern p = Pattern.compile(ROLE_WORKSPACE_MANAGER + "(\\d+)");
                Matcher m = p.matcher(role.getName());
                if (m.find()) {

                    String workspaceId = m.toMatchResult().group(0).replace(ROLE_WORKSPACE_MANAGER, "");
                    if (roleService.canAssignRoleWorkspaceManager(Long.valueOf(workspaceId), authentication)) {
                        roles.add(role);
                    }
                }
            }

            if (role.getName().startsWith(ROLE_WORKSPACE_MEMBER)) {
                Pattern p = Pattern.compile(ROLE_WORKSPACE_MEMBER + "(\\d+)");
                Matcher m = p.matcher(role.getName());
                if (m.find()) {

                    String workspaceId = m.toMatchResult().group(0).replace(ROLE_WORKSPACE_MEMBER, "");
                    if (roleService.canAssignRoleWorkspaceMember(Long.valueOf(workspaceId), authentication)) {
                        roles.add(role);
                    }
                }
            }
        }
        return roles;

    }

    public static String getWorkspaceManagerPrefix() {
        return ROLE_WORKSPACE_MANAGER;
    }

    public static String getWorkspaceMemberPrefix() {
        return ROLE_WORKSPACE_MEMBER;
    }

    public static String getWorkspaceMemberRole(Workspace workspace) {
        return ROLE_WORKSPACE_MEMBER + workspace.getId();
    }

    public static String getWorkspaceMemberRole(Long workspaceId) {
        return ROLE_WORKSPACE_MEMBER + workspaceId;
    }

    public static String getWorkspaceManagerRole(Workspace workspace) {
        return ROLE_WORKSPACE_MANAGER + workspace.getId();
    }

    public static String getWorkspaceManagerRole(Long workspaceId) {
        return ROLE_WORKSPACE_MANAGER + workspaceId;
    }

    public static String getRoleSiteManager() {
        return ROLE_SITE_MANAGER;
    }

    public static String getRoleDeploymentManager() {
        return ROLE_DEPLOYMENT_MANAGER;
    }

}
