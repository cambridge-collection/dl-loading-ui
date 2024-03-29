package uk.cam.lib.cdl.loading.forms;

import uk.cam.lib.cdl.loading.model.editor.*;
import uk.cam.lib.cdl.loading.model.editor.ui.UICollection;

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
    @NotBlank(message = "Must specify a url-slug for this collection.")
    @Pattern(regexp = "^[a-z\\-0-9_]+$", message = "Must be all lower case letters, numbers, underscore '_' or hyphen '-' with no spaces.")
    private String urlSlugName;

    // Can be null/blank for new collections.
    private String collectionId;

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

    private String fullDescriptionPath;
    @NotBlank(message = "Must specify a full description.")
    private String fullDescriptionHTML;

    private String proseCreditPath;

    private String proseCreditHTML;

    @NotBlank(message = "Must specify a collection type")
    private String collectionType;

    @NotNull
    private List<String> itemIds;

    @NotNull
    private List<String> subcollections;

    @NotBlank(message = "Must specify a thumbnail URL")
    private String thumbnailURL;

    public CollectionForm(String collectionId, Collection collection, String descriptionHTML, String creditHTML,
                          UICollection uiCollection) {
        if (collectionId == null || collection == null) {
            return;
        }
        this.urlSlugName = collection.getName().getUrlSlug();
        this.collectionId = collectionId;
        this.sortName = collection.getName().getSort();
        this.shortName = collection.getName().getShortName();
        this.fullName = collection.getName().getFull();
        this.shortDescription = collection.getDescription().getShortDescription();
        this.mediumDescription = collection.getDescription().getMedium();
        this.fullDescriptionPath = collection.getDescription().getFull().getId();
        this.fullDescriptionHTML = descriptionHTML;
        this.proseCreditPath = collection.getCredit().getProse().getId();
        this.proseCreditHTML = creditHTML;
        this.thumbnailURL = collection.getThumbnailURL();
        this.collectionType = uiCollection.getLayout();

        List<String> itemIds = new ArrayList<>();
        for (Id id : collection.getItemIds()) {
            if (id.getId()!=null && !id.getId().trim().equals("")) {
                itemIds.add(id.getId());
            }
        }
        this.itemIds = itemIds;

        List<String> sub_collections = new ArrayList<>();
        for (Id id : collection.getSubCollectionIds()) {
            if (id.getId()!=null && !id.getId().trim().equals("")) {
                sub_collections.add(id.getId());
            }
        }
        this.subcollections = sub_collections;
    }

    public CollectionForm() { }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getMediumDescription() {
        return mediumDescription;
    }

    public String getFullDescriptionPath() {
        return fullDescriptionPath;
    }

    public String getFullDescriptionHTML() {
        return fullDescriptionHTML;
    }

    public String getProseCreditPath() {
        return proseCreditPath;
    }

    public String getProseCreditHTML() {
        return proseCreditHTML;
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

    public String getCollectionId() {
        return collectionId;
    }

    public List<String> getItemIds() {
        return itemIds;
    }

    public List<String> getSubCollectionIds() {
        return subcollections;
    }

    public Collection toCollection() {
        CollectionName name = new CollectionName(urlSlugName, sortName, shortName, fullName);
        CollectionDescription description = new CollectionDescription(shortDescription, new Id(fullDescriptionPath),
            mediumDescription);
        CollectionCredit credit = new CollectionCredit(new Id(proseCreditPath));

        List<Id> itemIds = new ArrayList<>();
        for (String id : getItemIds()) {
            if (id!=null && !id.trim().equals("")) {
                itemIds.add(new Id(id));
            }
        }

        List<Id> subCollectionIds = new ArrayList<>();
        for (String id : getSubCollectionIds()) {
            if (id!=null && !id.trim().equals("")) {
                subCollectionIds.add(new Id(id));
            }
        }

        Collection c = new Collection(name, description, credit, itemIds, subCollectionIds);
        c.setThumbnailURL(thumbnailURL);
        c.setCollectionId(collectionId);
        return c;
    }

    public UICollection toUICollection() {

        return new UICollection(new Id(collectionId), collectionType, new Id(thumbnailURL));
    }

    public void setUrlSlugName(String urlSlugName) {

        this.urlSlugName = urlSlugName;
    }

    public void setCollectionId(String collectionId) {

        this.collectionId = collectionId;
    }

    public void setCollectionType(String collectionType) {
        this.collectionType = collectionType;
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

    public void setFullDescriptionPath(String fullDescriptionPath) {
        this.fullDescriptionPath = fullDescriptionPath;
    }

    public void setFullDescriptionHTML(String fullDescriptionHTML) {
        this.fullDescriptionHTML = fullDescriptionHTML;
    }

    public void setProseCreditPath(String proseCreditPath) {
        this.proseCreditPath = proseCreditPath;
    }

    public void setProseCreditHTML(String proseCreditHTML) {
        this.proseCreditHTML = proseCreditHTML;
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

    public void setSubCollectionIds(List<String> collectionIds) {

        // Thymeleaf appends extra [ ] to the itemIds, so remove these here.
        List<String> ids = new ArrayList<>();
        for (String id : collectionIds) {
            id = id.replaceAll("([\\[\\]])", "");
            ids.add(id);
        }

        this.subcollections = ids;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }
}

