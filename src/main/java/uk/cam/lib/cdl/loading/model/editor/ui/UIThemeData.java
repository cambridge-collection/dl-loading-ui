package uk.cam.lib.cdl.loading.model.editor.ui;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.beans.ConstructorProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UIThemeData {

    private final List<UICollection> collections;
    private final UIPage page;
    private final UIStaticPage staticPage;

    @ConstructorProperties({"collections", "pages", "static-pages"})
    public UIThemeData(List<UICollection> collections, UIPage page, UIStaticPage staticPage) {
        this.collections = collections;
        this.page = page;
        this.staticPage = staticPage;
    }

    public List<UICollection> getCollections() {
        return collections;
    }

    @JsonProperty("static-pages")
    public UIStaticPage getStaticPage() {
        return staticPage;
    }

    @JsonProperty("pages")
    public UIPage getPage() {
        return page;
    }
}
