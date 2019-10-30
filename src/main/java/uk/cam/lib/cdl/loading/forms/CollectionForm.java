package uk.cam.lib.cdl.loading.forms;

import uk.cam.lib.cdl.loading.model.editor.*;

import java.util.List;
import java.util.stream.Collectors;

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
        this.itemIds = collection.getItemIds().stream().map(Id::getId).collect(Collectors.toList());
        this.filepath = collection.getFilepath();
        this.thumbnailURL = collection.getThumbnailURL();
    }

    public CollectionForm() {
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription() {
        this.shortDescription = shortDescription;
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
        Collection c = new Collection(collectionType, name, description, credit,
            itemIds.stream().map(Id::new).collect(Collectors.toList()));
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

    public void setFullDescription(String fullDescription) {
        this.fullDescription = fullDescription;
    }

    public void setProseCredit(String proseCredit) {
        this.proseCredit = proseCredit;
    }

    public void setItemIds(List<String> itemIds) {
        this.itemIds = itemIds;
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
