package uk.cam.lib.cdl.loading.model.editor.ui;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.cam.lib.cdl.loading.model.editor.Id;

import java.beans.ConstructorProperties;

public class UIPage {

    private final String name;
    private final Id htmlPath;

    @ConstructorProperties({"name", "html"})
    public UIPage(String name, Id htmlPath) {
        this.name = name;
        this.htmlPath = htmlPath;
    }

    @JsonProperty("html")
    public Id getHtmlPath() {
        return htmlPath;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

}
