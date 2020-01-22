package uk.cam.lib.cdl.loading.model.editor.ui;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import uk.cam.lib.cdl.loading.model.editor.Id;

import java.beans.ConstructorProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UICollection {

    private final Id collection;
    private String layout;
    private Id thumbnail;

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

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public Id getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Id thumbnail) {
        this.thumbnail = thumbnail;
    }
}
