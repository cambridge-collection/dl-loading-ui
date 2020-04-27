package uk.cam.lib.cdl.loading.forms;

import org.apache.commons.lang.StringUtils;
import uk.cam.lib.cdl.loading.model.editor.Workspace;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// TODO validation
public class WorkspaceForm {

    private long id;
    @NotBlank(message = "Must specify a workspace name.")
    private String name;
    private List<String> collectionIds;
    private String itemIds;

    public WorkspaceForm() {
    }

    public WorkspaceForm(Workspace workspace) {

        this.id = workspace.getId();
        this.name = workspace.getName();
        this.collectionIds = workspace.getCollectionIds();
        this.itemIds = StringUtils.join(workspace.getItemIds(), ",");
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

    public String getItemIds() {
        return itemIds;
    }

    public void setItemIds(String itemIds) {
        this.itemIds = itemIds;
    }

    public Workspace toWorkspace() {
        Workspace workspace = new Workspace();
        workspace.setId(id);
        workspace.setName(name);
        workspace.setCollectionIds(collectionIds);

        // NOTE: We need a modifiable list here.
        List<String> list = new ArrayList<>();
        Collections.addAll(list, itemIds.split("\\s*,\\s*"));
        workspace.setItemIds(list);
        return workspace;
    }
}

