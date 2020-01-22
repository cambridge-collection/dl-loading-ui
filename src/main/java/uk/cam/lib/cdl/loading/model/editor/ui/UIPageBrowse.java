package uk.cam.lib.cdl.loading.model.editor.ui;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.cam.lib.cdl.loading.model.editor.Id;

import java.beans.ConstructorProperties;

public class UIPageBrowse {

    private Id html;

    @ConstructorProperties({"html"})
    public UIPageBrowse(Id html) {
        this.html = html;
    }

    @JsonProperty("html")
    public Id getHtml() {
        return html;
    }
}
