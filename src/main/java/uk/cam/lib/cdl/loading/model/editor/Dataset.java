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

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("class Dataset {\n");
        sb.append("    @type: ").append(toIndentedString(type)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    collections: ").append(toIndentedString(collections)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
