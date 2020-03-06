package uk.cam.lib.cdl.loading.model.editor;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

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

    @OneToMany(
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinColumn(name = "workspace_id")
    private List<WorkspaceCollectionMapping> collectionMappings;

    @OneToMany(
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinColumn(name = "workspace_id")
    private List<WorkspaceItemMapping> itemMappings;

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

    public List<WorkspaceCollectionMapping> getCollectionMappings() {
        return collectionMappings;
    }

    public void setCollectionMappings(List<WorkspaceCollectionMapping> collectionMappings) {
        this.collectionMappings = collectionMappings;
    }

    public List<WorkspaceItemMapping> getItemMappings() {
        return itemMappings;
    }

    public void setItemMappings(List<WorkspaceItemMapping> itemMappings) {
        this.itemMappings = itemMappings;
    }

    public List<String> getCollectionIds() {
        List<String> collectionIds = new ArrayList<>();
        for (WorkspaceCollectionMapping map: collectionMappings) {
            collectionIds.add(map.getCollectionId());
        }
        return collectionIds;
    }

    public List<String> getItemIds() {
        List<String> itemIds = new ArrayList<>();
        for (WorkspaceItemMapping map: itemMappings) {
            itemIds.add(map.getItemId());
        }
        return itemIds;
    }
}
