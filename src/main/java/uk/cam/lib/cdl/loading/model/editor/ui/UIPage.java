package uk.cam.lib.cdl.loading.model.editor.ui;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.cam.lib.cdl.loading.model.editor.Id;

import java.beans.ConstructorProperties;

public class UIPage {

    private final String name;
    private final Id html;
    private Id sidebarHtml;

    @ConstructorProperties({"name", "html"})
    public UIPage(String name, Id html) {
        this.name = name;
        this.html = html;
        this.sidebarHtml = null;
    }

    @JsonProperty("html")
    public Id getHtml() {
        return html;
    }

    // Optional.  May be null.
    @JsonProperty("sidebar-html")
    public Id getSidebarHtml() {
        return sidebarHtml;
    }

    @JsonProperty("sidebar-html")
    public void setSidebarHtml(Id sidebarHtml) {
        this.sidebarHtml = sidebarHtml;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

}
