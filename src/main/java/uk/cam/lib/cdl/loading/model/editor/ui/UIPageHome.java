package uk.cam.lib.cdl.loading.model.editor.ui;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.cam.lib.cdl.loading.model.editor.Id;

import java.beans.ConstructorProperties;
import java.util.List;

public class UIPageHome {

    private Id latestNewsHtml;
    private List<Id> carouselEntriesHTML;

    @ConstructorProperties({"latest-news-html", "carousel-enties-html"})
    public UIPageHome(Id latestNewsHtml, List<Id> carouselEntriesHTML) {
        this.latestNewsHtml = latestNewsHtml;
        this.carouselEntriesHTML = carouselEntriesHTML;
    }

    @JsonProperty("latest-news-html")
    public Id getLatestNewsHtml() {
        return latestNewsHtml;
    }

    @JsonProperty("carousel-enties-html")
    public List<Id> getCarouselEntriesHTML() {
        return carouselEntriesHTML;
    }
}
