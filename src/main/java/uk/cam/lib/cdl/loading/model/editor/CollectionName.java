package uk.cam.lib.cdl.loading.model.editor;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CollectionName {

    private String urlslug;
    private String sort;
    private String shortName;
    private String full;

    @JsonProperty("url-slug")
    public String getUrlSlug() {
        return urlslug;
    }

    @JsonProperty("url-slug")
    public void setUrlSlug(String urlslug) {
        this.urlslug = urlslug;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    @JsonProperty("short")
    public String getShortName() {
        return shortName;
    }

    @JsonProperty("short")
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getFull() {
        return full;
    }

    public void setFull(String full) {
        this.full = full;
    }
}
