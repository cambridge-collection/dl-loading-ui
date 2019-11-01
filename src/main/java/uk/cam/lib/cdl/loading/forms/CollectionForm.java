package uk.cam.lib.cdl.loading.forms;

import uk.cam.lib.cdl.loading.model.editor.*;

import java.util.ArrayList;
import java.util.List;

public class CollectionForm {

    private String collectionType;
    private String urlSlugName;
    private String sortName;
    private String shortName;
    private String fullName;
    private String shortDescription;
    private String mediumDescription;
    private String fullDescription;
    private String proseCredit;
    private List<String> itemIds;
    private String filepath;
    private String thumbnailURL;

    public CollectionForm(Collection collection) {
        this.collectionType = collection.getType();
        this.urlSlugName = collection.getName().getUrlSlug();
        this.sortName = collection.getName().getSort();
        this.shortName = collection.getName().getShortName();
        this.fullName = collection.getName().getFull();
        this.shortDescription = collection.getDescription().getShortDescription();
        this.mediumDescription = collection.getDescription().getMedium();
        this.fullDescription = collection.getDescription().getFull().getId();
        this.proseCredit = collection.getCredit().getProse().getId();
        this.filepath = collection.getFilepath();
        this.thumbnailURL = collection.getThumbnailURL();

        List<String> itemIds = new ArrayList<>();
        for (Id id : collection.getItemIds()) {
            itemIds.add(id.getId());
        }
        this.itemIds = itemIds;
    }

    public CollectionForm() {
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
        c.setFilepath(filepath);
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

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }
}
