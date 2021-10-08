package uk.cam.lib.cdl.loading.model.editor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import org.springframework.lang.Nullable;

import java.beans.ConstructorProperties;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static uk.cam.lib.cdl.loading.model.editor.ModelOps.ModelOps;

/**
 * TODO Make this a full implementation
 */
@JsonIgnoreProperties(value = {"@type"}, allowGetters = true)
public class Collection implements Comparable<Collection> {
    public static final String TYPE = "https://schemas.cudl.lib.cam.ac.uk/package/v1/collection.json";
    private final CollectionName name;
    @Nullable
    private final CollectionDescription description;
    @Nullable
    private final CollectionCredit credit;
    private final List<Id> ids;
    @Nullable
    private String thumbnailURL;
    @Nullable
    private String collectionId;
    private final List<Id> subcollections;

    @ConstructorProperties({"name", "description", "credit", "items", "collections"})
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Collection(@JsonProperty("name") CollectionName name,
                      @JsonProperty("description") CollectionDescription description,
                      @JsonProperty("credit") CollectionCredit credit,
                      @JsonProperty("items") List<Id> ids,
                      @JsonProperty("collections") List<Id> subcollections) {
        Preconditions.checkNotNull(name, "name cannot be null");
        Preconditions.checkNotNull(ids, "ids cannot be null");
        this.name = name;
        this.description = description;
        this.credit = credit;
        // Ensure ids is a mutable list
        this.ids = new ArrayList<>(ids);
        this.subcollections = new ArrayList<>(subcollections);
    }

    /**
     * Create a copy of a Collection.
     *
     * <p>Mutable members are deep-copied, while immutable members are reused.
     */
    public static Collection copyOf(Collection other) {
        Preconditions.checkNotNull(other);
        var copy = new Collection(
            other.name, // immutable
            other.description == null ? null : CollectionDescription.copyOf(other.description),
            other.credit == null ? null : CollectionCredit.copyOf(other.credit),
            other.ids, // ids  and sub-collections are cloned in the constructor
            other.subcollections
        );
        copy.setThumbnailURL(other.getThumbnailURL());
        copy.setCollectionId(other.getCollectionId());
        return copy;
    }

    @JsonProperty("@type")
    protected String getType() {
        return TYPE;
    }

    public CollectionName getName() {
        return name;
    }

    @Nullable
    public CollectionDescription getDescription() {
        return description;
    }

    @Nullable
    public CollectionCredit getCredit() {
        return credit;
    }

    @JsonProperty("items")
    public List<Id> getItemIds() {
        return ids;
    }

    @JsonProperty("collections")
    public List<Id> getSubCollectionIds() {
        return subcollections;
    }

    @JsonIgnore
    @Nullable
    public String getThumbnailURL() {
        return this.thumbnailURL;
    }

    @JsonIgnore
    public void setThumbnailURL(@Nullable String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    public String toString() {
        return toString(true);
    }

    private String toString(boolean includeId) {
        StringBuffer sb = new StringBuffer();
        sb.append("class Collection {\n");

        if(includeId && this.collectionId != null) {
            sb.append("    @id: ").append(toIndentedString(collectionId)).append("\n");
        }
        sb.append("    @type: ").append(toIndentedString(getType())).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    credit: ").append(toIndentedString(credit)).append("\n");
        sb.append("    items: ").append(toIndentedString(ids)).append("\n");
        sb.append("    collections: ").append(toIndentedString(subcollections)).append("\n");
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
        Preconditions.checkNotNull(collection);
        var id = this.collectionId;
        var otherId = collection.collectionId;
        Preconditions.checkState(id != null && otherId != null, "Cannot compare Collections with no collectionId set");
        return id.compareTo(otherId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Collection) {
            // Ignore any collection id for equality/hash
            return toString(false).equals(((Collection)obj).toString(false));
        }
        return false;
    }

    @Override
    public int hashCode() {
        // Ignore any collection id for equality/hash
        return toString(false).hashCode();
    }

    @JsonIgnore
    @Nullable
    public String getCollectionId() {
        return collectionId;
    }

    @JsonIgnore
    public Path getIdAsPath() {
        Preconditions.checkNotNull(collectionId, "collection has no ID set");
        return ModelOps().validatePathForId(Path.of(collectionId));
    }

    @JsonIgnore
    public void setCollectionId(@Nullable String collectionId) {
        this.collectionId = collectionId;
    }
}
