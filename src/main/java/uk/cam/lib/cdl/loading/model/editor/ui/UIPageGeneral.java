package uk.cam.lib.cdl.loading.model.editor.ui;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.cam.lib.cdl.loading.model.editor.Id;

import java.beans.ConstructorProperties;

public class UIPageGeneral {

    private Id defaultThumbnail;
    private Id defaultCollectionImage;

    @ConstructorProperties({"default-thumbnail", "default-collection-image"})
    public UIPageGeneral(Id defaultThumbnail, Id defaultCollectionImage) {
        this.defaultThumbnail = defaultThumbnail;
        this.defaultCollectionImage = defaultCollectionImage;
    }

    @JsonProperty("default-thumbnail")
    public Id getDefaultThumbnail() {
        return defaultThumbnail;
    }

    @JsonProperty("default-collection-image")
    public Id getDefaultCollectionImage() {
        return defaultCollectionImage;
    }
}
