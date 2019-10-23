package uk.cam.lib.cdl.loading.model.editor;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CollectionDescription {

    private String shortDescription;
    private String medium;
    private Id full;

    @JsonProperty("short")
    public String getShortDescription() {
        return shortDescription;
    }

    @JsonProperty("short")
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public Id getFull() {
        return full;
    }

    public void setFull(Id full) {
        this.full = full;
    }

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("class CollectionDescription {\n");
        sb.append("    short: ").append(toIndentedString(shortDescription)).append("\n");
        sb.append("    medium: ").append(toIndentedString(medium)).append("\n");
        sb.append("    full: ").append(toIndentedString(full)).append("\n");
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
