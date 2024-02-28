package uk.cam.lib.cdl.loading.model.editor.ui;

import java.util.HashMap;
import java.util.List;

public class UIThemeUI {

    protected String title;
    protected String description;
    protected String attribution;
    protected List<String> css;
    protected List<String> js;
    protected HashMap<String, UIThemeUIImage> images;

    public UIThemeUI() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAttribution() {
        return attribution;
    }

    public void setAttribution(String attribution) {
        this.attribution = attribution;
    }

    public List<String> getCss() {
        return css;
    }

    public void setCss(List<String> css) {
        this.css = css;
    }

    public List<String> getJs() {
        return js;
    }

    public void setJs(List<String> js) {
        this.js = js;
    }

    public HashMap<String, UIThemeUIImage> getImages() {
        return images;
    }

    public void setImages(HashMap<String, UIThemeUIImage> images) {
        this.images = images;
    }
}
