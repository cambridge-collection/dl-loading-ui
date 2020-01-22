package uk.cam.lib.cdl.loading.model.editor.ui;

import java.beans.ConstructorProperties;

public class UIPage {

    private UIPageGeneral general;
    private UIPageHome home;
    private UIPageAbout about;
    private UIPageBrowse browse;

    @ConstructorProperties({"general", "home", "about", "browse"})
    public UIPage(UIPageGeneral general, UIPageHome home, UIPageAbout about, UIPageBrowse browse) {
        this.general = general;
        this.home = home;
        this.about = about;
        this.browse = browse;
    }

    public UIPageGeneral getGeneral() {
        return general;
    }

    public UIPageHome getHome() {
        return home;
    }

    public UIPageAbout getAbout() {
        return about;
    }

    public UIPageBrowse getBrowse() {
        return browse;
    }
}
