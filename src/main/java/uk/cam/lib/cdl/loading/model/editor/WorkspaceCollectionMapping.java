package uk.cam.lib.cdl.loading.model.editor;

import javax.persistence.*;
import javax.persistence.Id;
import java.io.Serializable;

@Entity(name = "WorkspaceCollectionMapping")
@Table(name = "collections_in_workspaces")
public class WorkspaceCollectionMapping implements Serializable {

    private static final long serialVersionUID = 6100983612195161767L;

    @Id
    @ManyToOne
    @JoinColumn(name ="workspace_id")
    private Workspace workspace;

    @Id
    private String collectionId;

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }
}
