package uk.cam.lib.cdl.loading.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import uk.cam.lib.cdl.loading.dao.WorkspaceRepository;
import uk.cam.lib.cdl.loading.model.editor.Workspace;
import uk.cam.lib.cdl.loading.utils.RoleHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class RoleService {


    @Autowired
    private WorkspaceRepository workspaceRepository;

    // TODO
    // User management - only site manager can assign any role
    // User management - workspace manager can assign roles in workspace

    private boolean hasRoleRegex(String regex, Authentication authentication) {

        Pattern p = Pattern.compile(regex);
        if (authentication!=null) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                if (p.matcher(authority.getAuthority()).matches()) {
                    return true;
                }
            }
        }
        return false;

    }

    // Users who are manager or member of a workspace can edit collections that are in that workspace.
    // Anyone who can view a collection can edit it.
    public boolean canEditCollection(String collectionid, Authentication authentication) {

        // check that user can view at least one of these workspaces
        for (GrantedAuthority authority : authentication.getAuthorities()) {

            if (authority.getAuthority().startsWith("ROLE_")) { // TODO get prefix

                List<Workspace> workspaces = workspaceRepository.findWorkspaceByCollectionIds(collectionid);
                for (Workspace workspace : workspaces) {
                    String workspaceManagerRole = RoleHelper.getWorkspaceManagerRole(workspace);
                    String workspaceMemberRole = RoleHelper.getWorkspaceMemberRole(workspace);
                    if (workspace.getCollectionIds().contains(collectionid)) {
                        if (authority.getAuthority().equals(workspaceManagerRole) ||
                            authority.getAuthority().equals(workspaceMemberRole)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    // Users who have workspace manger role (for that workspace) or site manager role
    public boolean canEditWorkspace(Long workspaceId, Authentication authentication) {
        List<Long> workspaceIds = new ArrayList<>();
        workspaceIds.add(workspaceId);
        return canEditWorkspace(workspaceIds, authentication);
    }

    // Users who have workspace manager role (for that workspace) or site manager role
    // Returns true if the user can edit at least one of the workspace Ids listed.
    // This is used when editing an item or collection which is in multiple workspaces.
    public boolean canEditWorkspace(List<Long> workspaceIds, Authentication authentication) {

        if (workspaceIds==null) {
            return authentication.getAuthorities().contains(new SimpleGrantedAuthority(RoleHelper.getRoleSiteManager()));
        }

        for (Long workspaceId: workspaceIds) {
            // does user have manager role for this workspace
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                if (authority.getAuthority().equals(RoleHelper.getWorkspaceManagerRole(workspaceId)) ||
                    authority.getAuthority().equals(RoleHelper.getRoleSiteManager())) {
                    return true;
                }
            }
        }
        return false;
    }


    // ROLE_WORKSPACE_MEMBER\\d+ or ROLE_WORKSPACE_MANGER\\d+
    public boolean canViewWorkspaces(Authentication authentication) {
        return hasRoleRegex(RoleHelper.getWorkspaceMemberPrefix()+"\\d+", authentication)
            || hasRoleRegex(RoleHelper.getWorkspaceManagerPrefix()+"\\d+", authentication);

    }

    public boolean canEditWorkspaces(Authentication authentication) {
        return hasRoleRegex(RoleHelper.getWorkspaceManagerPrefix()+"\\d+", authentication) ||
            hasRoleRegex(RoleHelper.getRoleSiteManager(), authentication);
    }

    public boolean canViewWorkspace(Long workspaceId, Authentication authentication) {
        return hasRoleRegex(RoleHelper.getWorkspaceMemberPrefix()+workspaceId, authentication) ||
            hasRoleRegex(RoleHelper.getWorkspaceManagerPrefix()+workspaceId, authentication);
    }

    public boolean canDeploySites(Authentication authentication) {
        return hasRoleRegex(RoleHelper.getRoleDeploymentManager(), authentication);
    }

    public boolean canBuildPackages(Authentication authentication) {
        return hasRoleRegex(RoleHelper.getRoleDeploymentManager(), authentication);
    }

    public boolean canAssignRoleDeploymentManager(Authentication authentication) {
        return hasRoleRegex(RoleHelper.getRoleSiteManager(), authentication);
    }

    public boolean canAddWorkspaces(Authentication authentication) {
        return hasRoleRegex(RoleHelper.getRoleSiteManager(), authentication);
    }

/*    public boolean canAssignRoleWorkspaceManager(Authentication authentication) {
        return hasRoleRegex(RoleHelper.getRoleSiteManager(), authentication);
    }*/

}
