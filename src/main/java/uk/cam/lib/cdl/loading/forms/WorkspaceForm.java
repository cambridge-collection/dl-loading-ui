package uk.cam.lib.cdl.loading.forms;

import uk.cam.lib.cdl.loading.model.editor.Workspace;
import uk.cam.lib.cdl.loading.model.editor.WorkspaceCollectionMapping;
import uk.cam.lib.cdl.loading.model.editor.WorkspaceItemMapping;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

public class WorkspaceForm {

    private long id;

    @NotBlank(message = "Must specify a workspace name.")
    private String name;

    private List<String> collectionIds;

    private List<String> itemIds;

    public WorkspaceForm() {
    }

    public WorkspaceForm(Workspace workspace) {
        this.id = workspace.getId();
        this.name = workspace.getName();
        this.collectionIds = workspace.getCollectionIds();
        this.itemIds = workspace.getItemIds();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getCollectionIds() {
        return collectionIds;
    }

    public void setCollectionIds(List<String> collectionIds) {
        this.collectionIds = collectionIds;
    }

    public List<String> getItemIds() {
        return itemIds;
    }

    public void setItemIds(List<String> itemIds) {
        this.itemIds = itemIds;
    }

    public Workspace toWorkspace() {
        Workspace workspace = new Workspace();
        workspace.setName(name);
        workspace.setId(id);
        List<WorkspaceCollectionMapping> collectionMappings = new ArrayList<>();
        for (String collectionId: collectionIds) {
            WorkspaceCollectionMapping mapping = new WorkspaceCollectionMapping();
            mapping.setCollectionId(collectionId);
            mapping.setWorkspace(workspace);
            collectionMappings.add(mapping);
        }
        workspace.setCollectionMappings(collectionMappings);
        List<WorkspaceItemMapping> itemMappings = new ArrayList<>();
        for (String itemId: itemIds) {
            WorkspaceItemMapping mapping = new WorkspaceItemMapping();
            mapping.setItemId(itemId);
            mapping.setWorkspace(workspace);
            itemMappings.add(mapping);
        }
        workspace.setItemMappings(itemMappings);
        return workspace;
    }
}

