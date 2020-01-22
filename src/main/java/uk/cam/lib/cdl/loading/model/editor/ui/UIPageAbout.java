package uk.cam.lib.cdl.loading.model.editor.ui;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.cam.lib.cdl.loading.model.editor.Id;

import java.beans.ConstructorProperties;

public class UIPageAbout {

    private Id html;
    private Id sidebarHtml;

    @ConstructorProperties({"html", "sidebar-html"})
    public UIPageAbout(Id html, Id sidebarHtml) {
        this.html = html;
        this.sidebarHtml = sidebarHtml;
    }

    @JsonProperty("html")
    public Id getHtml() {
        return html;
    }

    @JsonProperty("sidebar-html")
    public Id getSidebarHtml() {
        return sidebarHtml;
    }
}
