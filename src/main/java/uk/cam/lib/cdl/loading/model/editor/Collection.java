package uk.cam.lib.cdl.loading.model.editor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * TODO Make this a full implementation
 */
public class Collection {

    private final String type;
    private final CollectionName name;
    private final CollectionDescription description;
    private final CollectionCredit credit;
    private final List<Id> ids;
    private String filepath;
    private String thumbnailURL;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Collection(@JsonProperty("@type") String type,
                      @JsonProperty("name") CollectionName name,
                      @JsonProperty("description") CollectionDescription description,
                      @JsonProperty("credit") CollectionCredit credit,
                      @JsonProperty("items") List<Id> ids) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.credit = credit;
        this.ids = ids;
    }

    @JsonProperty("@type")
    public String getType() {
        return type;
    }

    public CollectionName getName() {
        return name;
    }

    public CollectionDescription getDescription() {
        return description;
    }

    public CollectionCredit getCredit() {
        return credit;
    }

    @JsonProperty("items")
    public List<Id> getItemIds() {
        return ids;
    }

    @JsonIgnore
    public String getFilepath() {
        return filepath;
    }

    @JsonIgnore
    public void setFilepath(String filepath) {
        if (this.filepath == null) {
            this.filepath = filepath;
        }
    }

    @JsonIgnore
    public String getThumbnailURL() {
        return this.thumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("class Collection {\n");

        sb.append("    @type: ").append(toIndentedString(type)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    credit: ").append(toIndentedString(credit)).append("\n");
        sb.append("    items: ").append(toIndentedString(ids)).append("\n");
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
