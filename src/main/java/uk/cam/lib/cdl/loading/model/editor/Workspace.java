package uk.cam.lib.cdl.loading.model.editor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "Workspace")
@Table(name = "workspaces")
public class Workspace {

    @javax.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true, name = "workspace_id")
    private long id;

    @Column(nullable = false, name = "name")
    private String name;

    @ElementCollection
    @CollectionTable(
        name="collections_in_workspaces",
        joinColumns=@JoinColumn(name="workspace_id")
    )
    @Column(name="collection_id")
    private List<String> collectionIds = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
        name="items_in_workspaces",
        joinColumns=@JoinColumn(name="workspace_id")
    )
    @Column(name="item_id")
    private List<String> itemIds = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long workspaceId) {
        this.id = workspaceId;
    }

    public List<String> getItemIds() {
        return itemIds;
    }

    public void setItemIds(List<String> itemIds) {
        this.itemIds = itemIds;
    }

    public List<String> getCollectionIds() {
        return collectionIds;
    }

    public void setCollectionIds(List<String> collectionIds) {
        this.collectionIds = collectionIds;
    }
}
