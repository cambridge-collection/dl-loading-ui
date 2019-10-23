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

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("class CollectionName {\n");
        sb.append("    short: ").append(toIndentedString(shortName)).append("\n");
        sb.append("    url-slug: ").append(toIndentedString(urlslug)).append("\n");
        sb.append("    full: ").append(toIndentedString(full)).append("\n");
        sb.append("    sort: ").append(toIndentedString(sort)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
