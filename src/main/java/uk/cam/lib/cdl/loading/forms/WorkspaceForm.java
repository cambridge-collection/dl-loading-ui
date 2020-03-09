package uk.cam.lib.cdl.loading.forms;

import uk.cam.lib.cdl.loading.model.editor.Workspace;

import javax.validation.constraints.NotBlank;
import java.util.List;

public class WorkspaceForm {

    private Workspace workspace;

    public WorkspaceForm() {
        workspace = new Workspace();
    }

    public WorkspaceForm(Workspace workspace) {
        this.workspace = workspace;
    }

    public long getId() {
        return workspace.getId();
    }

    public void setId(long id) {
        workspace.setId(id);
    }

    public String getName() {
        return workspace.getName();
    }

    public void setName(@NotBlank(message = "Must specify a workspace name.") String name) {
        workspace.setName(name);
    }


    public List<String> getCollectionIds() {
        return workspace.getCollectionIds();
    }

    public void setCollectionIds(List<String> collectionIds) {
        workspace.setCollectionIds(collectionIds);
    }

    public List<String> getItemIds() {
        return workspace.getItemIds();
    }

    public void setItemIds(List<String> itemIds) {
        workspace.setItemIds(itemIds);
    }

    public Workspace toWorkspace() {
        return workspace;
    }
}

