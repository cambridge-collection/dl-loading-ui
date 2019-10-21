package uk.cam.lib.cdl.loading.model.editor;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Dataset {

    private String type;
    private String name;
    private List<Id> collections;

    @JsonProperty("@type")
    public String getType() {
        return type;
    }

    @JsonProperty("@type")
    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Id> getCollections() {
        return collections;
    }

    public void setCollections(List<Id> collections) {
        this.collections = collections;
    }
}
