package uk.cam.lib.cdl.loading.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import uk.cam.lib.cdl.loading.dao.WorkspaceRepository;
import uk.cam.lib.cdl.loading.model.editor.Workspace;
import uk.cam.lib.cdl.loading.utils.RoleHelper;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class RoleService {


    @Autowired
    private WorkspaceRepository workspaceRepository;

    // TODO
    // Map Workspace member permissions to collections
    // Map workspace member permissions to items
    // Map workspace manager permissions too
    // Do annotations
    // Get buttons to grey out when not got permissions
    // User management - site manager can add any role
    // User management - workspace manager can assign roles in workspace
    // Anyone with at least one manager role can view user management section.

    // canEditCollection(collectionid, auth)
    // canEditItem(itemId, auth)
    // canEditWorkspace(workspaceId, auth)
    // canAssignUserToRole(role, auth)
    // canAccessUserManagement

    // TODO get ROLE prefix properly.

    public boolean hasRoleRegex(String regex, Authentication authentication) {

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

    // Users who are part of a workspace can edit collections that are in that workspace.
    // Anyone who can view a collection can edit it.
    public boolean canEditCollection(String collectionid, Authentication authentication) {

        // check what workspaces collectionid is in
        List<Workspace> workspaces = workspaceRepository.findWorkspaceByCollectionIds(collectionid);
        RoleHelper r = new RoleHelper(workspaceRepository);

        // check that user can view at least one of these workspaces
        for (GrantedAuthority authority : authentication.getAuthorities()) {

            if (authority.getAuthority().startsWith("ROLE_" + RoleHelper.WorkspaceRolesPrefix.WORKSPACE_MEMBER.toString()) ||
                authority.getAuthority().startsWith("ROLE_" + RoleHelper.WorkspaceRolesPrefix.WORKSPACE_MANAGER.toString())) {

                for (Workspace workspace : workspaces) {
                    String workspaceManagerRole = r.getWorkspaceManagerRole(workspace);
                    String workspaceMemberRole = r.getWorkspaceMemberRole(workspace);
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

    public boolean canEditWorkspace(List<Long> workspaceIds, Authentication authentication) {

        if (workspaceIds==null) { return false; }

        for (long workspaceId: workspaceIds) {
            String workspaceManagerRole = "ROLE_" + RoleHelper.WorkspaceRolesPrefix.WORKSPACE_MANAGER.toString() + workspaceId;
            // does user have manager role for this workspace
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                if (authority.getAuthority().equals(workspaceManagerRole)) {
                    return true;
                }
            }
        }
        return false;
    }



}
