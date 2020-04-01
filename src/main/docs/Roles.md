# User Management for Loading UI

## Restricting access by workspace

This allows workspace managers to create workspaces, and add any collections or items into that workspace.  
They could also add users to be members of the workspace and have permission to edit
 any item or collection that is contained within that workspace. 

### Roles

| ROLE       	                            | Description                                      	                                                                            | 
|----------------------------------------	|-----------------------------------------------------------------------------------------------------------------------------	|
| ROLE_SITE_MANAGER                         | Allows creating new workspaces.  Allows assigning users to any role.  Allows creating/editing users.                          |
| ROLE_WORKSPACE_MANAGER<Workspace_Id>  	| Allows assigning ROLE_WORKSPACE_MEMBER.   Allows adding any collection or item to workspace. Allows creating/editing users.   |
| ROLE_WORKSPACE_MEMBER<Workspace_Id>       | Allows editing of any collection or item within the workspace with id <Workspace_Id>.                                        	|
| ROLE_DEPLOYMENT_MANAGER 	                | Allows permission to trigger a package to be built.  Allows permission to deploy to any instance. 	                        |

In the case where you only want a user to edit one item you would need to create a workspace containing only that item 
and give the user membership of that workspace. 

Note: More than one workspace can contain an item or collection and edits made in other workspaces are reflected in all
 workspaces that contain that item/collection.

### Access to website sections

#### Edit Section
To access the 'edit' section a user must have at least one of the roles: ROLE_WORKSPACE_MANAGER<Workspace_Id> or 
ROLE_WORKSPACE_MEMBER<Workspace_Id>.  They may then access/edit only the workspaces with the id <Workspace_Id>.

#### Deploy and Packaging Sections
To access the 'deploy' or 'packaging' sections a user must have the ROLE_DEPLOYMENT_MANAGER.

#### User Management
To access the 'user management' section a user must have at least one of the roles: ROLE_WORKSPACE_MANAGER<Workspace_Id>
 or ROLE_SITE_MANAGER. 
 
 ##### Editing Users
 Anyone with access to this section can add/delete or edit users. Users with role 
 ROLE_SITE_MANAGER can give users any role.  Users with the ROLE_WORKSPACE_MANAGER<Workspace_Id> can only give
 users the ROLE_WORKSPACE_MEMBER<Workspace_Id> for the same workspace.

 ##### Editing Workspaces
 Users with ROLE_WORKSPACE_MANAGER<Workspace_Id> can edit the workspace with that id. 
 Users with ROLE_SITE_MANAGER can edit any workspace, and add/delete workspaces.
