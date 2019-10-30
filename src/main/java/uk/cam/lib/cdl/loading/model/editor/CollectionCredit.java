package uk.cam.lib.cdl.loading.model.editor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.beans.ConstructorProperties;

public class CollectionCredit {

    private final Id prose;

    @ConstructorProperties({"prose"})
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public CollectionCredit(@JsonProperty("prose") Id prose) {
        this.prose = prose;
    }

    public Id getProse() {
        return prose;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("class CollectionCredit {\n");
        sb.append("    prose: ").append(toIndentedString(prose)).append("\n");
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
