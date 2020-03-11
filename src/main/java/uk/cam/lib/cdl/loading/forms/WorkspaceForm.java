package uk.cam.lib.cdl.loading.forms;

import org.apache.commons.lang.StringUtils;
import uk.cam.lib.cdl.loading.model.editor.Workspace;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// TODO validation
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

    public String getItemIds() {
        return StringUtils.join(workspace.getItemIds(), ",");
    }

    public void setItemIds(String itemIds) {
        // NOTE: Do not replace with Arrays.asList as we need a modifiable list here.
        List<String> list = new ArrayList<>();
        Collections.addAll(list, itemIds.split("\\s*,\\s*"));
        workspace.setItemIds(list);
    }

    public Workspace toWorkspace() {
        return workspace;
    }
}

