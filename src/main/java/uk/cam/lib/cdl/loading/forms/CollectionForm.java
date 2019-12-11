package uk.cam.lib.cdl.loading.forms;

import uk.cam.lib.cdl.loading.model.editor.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * Note: This is currently more restrictive than the schema, needing not null values for all fields.
 * Also, values for restricting fields are a bit of a work in progress.
 */

public class CollectionForm {

    //@NotBlank(message = "Must specify collection type.")
    private String collectionType;

    @NotBlank(message = "Must specify a url-slug for this collection.")
    @Pattern(regexp = "^[a-z\\-]+$", message = "Must be all lower case letters or hyphen '-' with no spaces.")
    private String urlSlugName;

    @NotBlank(message = "Must specify a sort name.")
    private String sortName;

    @NotBlank(message = "Must specify a short name.")
    @Size(min = 2, max = 50, message = "Short Name should be between 2 and 50 characters.")
    private String shortName;

    @NotBlank(message = "Must specify a full name.")
    private String fullName;

    @NotBlank(message = "Must specify a short description.")
    @Size(min = 2, max = 500, message = "Short description should be between 2 and 500 characters.")
    private String shortDescription;

    @NotBlank(message = "Must specify a medium description.")
    @Size(min = 2, max = 5000, message = "Medium description must be between 2 and 5000 characters.")
    private String mediumDescription;

    //@NotBlank(message = "Must specify a full description.")
    private String fullDescription;

    //@NotBlank(message = "Must specify a prose credit.")
    private String proseCredit;

    @NotNull
    private List<String> itemIds;

    //@NotBlank(message = "Must specify a thumbnail URL")
    private String thumbnailURL;

    public CollectionForm(Collection collection) {
        if (collection == null) {
            return;
        }
        this.collectionType = collection.getType();
        this.urlSlugName = collection.getName().getUrlSlug();
        this.sortName = collection.getName().getSort();
        this.shortName = collection.getName().getShortName();
        this.fullName = collection.getName().getFull();
        this.shortDescription = collection.getDescription().getShortDescription();
        this.mediumDescription = collection.getDescription().getMedium();
        this.fullDescription = collection.getDescription().getFull().getId();
        this.proseCredit = collection.getCredit().getProse().getId();
        this.thumbnailURL = collection.getThumbnailURL();

        List<String> itemIds = new ArrayList<>();
        for (Id id : collection.getItemIds()) {
            itemIds.add(id.getId());
        }
        this.itemIds = itemIds;
    }

    public CollectionForm() {

        this.setCollectionType("https://schemas.cudl.lib.cam.ac.uk/package/v1/collection.json");
        this.setProseCredit("../pages/html/collections/sample/sponsors.html"); // TODO fix hardcoding
        this.setFullDescription("../pages/html/collections/sample/summary.html"); // TODO fix hardcoding
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getMediumDescription() {
        return mediumDescription;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public String getProseCredit() {
        return proseCredit;
    }

    public String getFullName() {
        return fullName;
    }

    public String getShortName() {
        return shortName;
    }

    public String getSortName() {
        return sortName;
    }

    public String getUrlSlugName() {
        return urlSlugName;
    }

    public String getCollectionType() {
        return collectionType;
    }

    public List<String> getItemIds() {
        return itemIds;
    }

    public Collection toCollection() {
        CollectionName name = new CollectionName(urlSlugName, sortName, shortName, fullName);
        CollectionDescription description = new CollectionDescription(shortDescription, new Id(fullDescription),
            mediumDescription);
        CollectionCredit credit = new CollectionCredit(new Id(proseCredit));

        List<Id> itemIds = new ArrayList<>();
        for (String id : getItemIds()) {
            itemIds.add(new Id(id));
        }
        Collection c = new Collection(collectionType, name, description, credit, itemIds);
        c.setThumbnailURL(thumbnailURL);
        return c;
    }

    public void setCollectionType(String collectionType) {
        this.collectionType = collectionType;
    }

    public void setUrlSlugName(String urlSlugName) {

        this.urlSlugName = urlSlugName;
    }

    public void setSortName(String sortName) {
        this.sortName = sortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setMediumDescription(String mediumDescription) {
        this.mediumDescription = mediumDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public void setFullDescription(String fullDescription) {
        this.fullDescription = fullDescription;
    }

    public void setProseCredit(String proseCredit) {
        this.proseCredit = proseCredit;
    }

    public void setItemIds(List<String> itemIds) {

        // Thymeleaf appends extra [ ] to the itemIds, so remove these here.
        List<String> ids = new ArrayList<>();
        for (String id : itemIds) {
            id = id.replaceAll("([\\[\\]])", "");
            ids.add(id);
        }

        this.itemIds = ids;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }
}
