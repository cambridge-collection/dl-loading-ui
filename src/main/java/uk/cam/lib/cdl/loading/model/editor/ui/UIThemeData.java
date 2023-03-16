package uk.cam.lib.cdl.loading.model.editor.ui;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.beans.ConstructorProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UIThemeData {

    private final List<UICollection> collections;
    private final List<UIPage> pages;

    @ConstructorProperties({"collections", "pages"})
    public UIThemeData(List<UICollection> collections, List<UIPage> pages) {
        this.collections = collections;
        this.pages = pages;
    }

    public List<UICollection> getCollections() {
        return collections;
    }

    @JsonProperty("pages")
    public List<UIPage> getPages() {
        return pages;
    }
}
