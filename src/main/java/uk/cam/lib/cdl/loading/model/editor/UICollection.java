package uk.cam.lib.cdl.loading.model.editor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.beans.ConstructorProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UICollection {

    private final Id collection;
    private final String layout;
    private final Id thumbnail;

    @ConstructorProperties({"collection", "layout", "thumbnail"})
    public UICollection(Id collection, String layout, Id thumbnail) {

        this.collection = collection;
        this.layout = layout;
        this.thumbnail = thumbnail;

    }

    public Id getCollection() {
        return collection;
    }

    public String getLayout() {
        return layout;
    }

    public Id getThumbnail() {
        return thumbnail;
    }
}
