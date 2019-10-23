package uk.cam.lib.cdl.loading.model.editor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO Make this a full implementation
 */
public class Collection {

    private String filepath;
    private String type;
    private CollectionName name;
    private CollectionDescription description;
    private CollectionCredit credit;
    private List<Id> ids = new ArrayList<>();
    private List<Item> items = new ArrayList<>();

    @JsonProperty("@type")
    public String getType() {
        return type;
    }

    @JsonProperty("@type")
    public void setType(String type) {
        this.type = type;
    }

    public CollectionName getName() {
        return name;
    }

    public void setName(CollectionName name) {
        this.name = name;
    }


    public CollectionDescription getDescription() {
        return description;
    }

    public void setDescription(CollectionDescription description) {
        this.description = description;
    }

    public CollectionCredit getCredit() {
        return credit;
    }

    public void setCredit(CollectionCredit credit) {
        this.credit = credit;
    }

    @JsonProperty("items")
    public List<Id> getItemIds() {
        return ids;
    }

    @JsonProperty("items")
    public void setItemIds(List<Id> ids) {
        this.ids = ids;
    }

    @JsonIgnore
    public String getFilepath() {
        return filepath;
    }

    @JsonIgnore
    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    @JsonIgnore
    public List<Item> getItems() {
        return items;
    }

    @JsonIgnore
    public void setItems(List<Item> items) {
        this.items = items;
    }

    // TODO fix this hardcoding
    @JsonIgnore
    public String getThumbnailURL() {
        return "/pages/images/collectionsView/collection-" + this.getName().getUrlSlug() + ".jpg";
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("class Collection {\n");

        sb.append("    @type: ").append(toIndentedString(type)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    credit: ").append(toIndentedString(credit)).append("\n");
        sb.append("    items: ").append(toIndentedString(items)).append("\n");
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
