package uk.cam.lib.cdl.loading.model.editor.ui;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * This class represents an theme image as defined in the cudl.ui.json5 file.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UIThemeUIImage {

    protected String src;
    protected String alt;
    protected String href;

    public UIThemeUIImage() {
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getAlt() {
        return alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }
}
