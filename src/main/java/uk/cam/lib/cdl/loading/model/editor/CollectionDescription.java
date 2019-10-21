package uk.cam.lib.cdl.loading.model.editor;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CollectionDescription {

    private String shortDescription;
    private String medium;
    private Id full;

    @JsonProperty("short")
    public String getShortDescription() {
        return shortDescription;
    }

    @JsonProperty("short")
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public Id getFull() {
        return full;
    }

    public void setFull(Id full) {
        this.full = full;
    }

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }
}
