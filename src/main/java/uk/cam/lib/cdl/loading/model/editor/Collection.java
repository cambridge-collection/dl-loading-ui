package uk.cam.lib.cdl.loading.model.editor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.beans.ConstructorProperties;
import java.util.List;

/**
 * TODO Make this a full implementation
 */
@JsonIgnoreProperties(value = {"@type"}, allowGetters = true)
public class Collection implements Comparable<Collection> {
    public static final String TYPE = "https://schemas.cudl.lib.cam.ac.uk/package/v1/collection.json";
    private final CollectionName name;
    private final CollectionDescription description;
    private final CollectionCredit credit;
    private final List<Id> ids;
    private String thumbnailURL;
    private String collectionId;

    @ConstructorProperties({"name", "description", "credit", "items"})
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Collection(@JsonProperty("name") CollectionName name,
                      @JsonProperty("description") CollectionDescription description,
                      @JsonProperty("credit") CollectionCredit credit,
                      @JsonProperty("items") List<Id> ids) {
        this.name = name;
        this.description = description;
        this.credit = credit;
        this.ids = ids;
    }

    @JsonProperty("@type")
    protected String getType() {
        return TYPE;
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
    public String getThumbnailURL() {
        return this.thumbnailURL;
    }

    @JsonIgnore
    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("class Collection {\n");

        sb.append("    @type: ").append(toIndentedString(getType())).append("\n");
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

    @Override
    public int compareTo(Collection collection) {
        return getCollectionId().compareTo(collection.getCollectionId());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Collection) {
            return toString().equals(obj.toString());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @JsonIgnore
    public String getCollectionId() {
        return collectionId;
    }

    @JsonIgnore
    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }
}
