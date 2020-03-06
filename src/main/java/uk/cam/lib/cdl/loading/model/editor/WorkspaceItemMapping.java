package uk.cam.lib.cdl.loading.model.editor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

@Entity(name = "WorkspaceItemMapping")
@Table(name = "items_in_workspaces")
public class WorkspaceItemMapping implements Serializable {

    private static final long serialVersionUID = -5681308232081936029L;

    @Id
    @ManyToOne
    @JoinColumn(name ="workspace_id")
    private Workspace workspace;

    @Id
    private String itemId;

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
}
