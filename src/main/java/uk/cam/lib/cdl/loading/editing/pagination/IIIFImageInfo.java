package uk.cam.lib.cdl.loading.editing.pagination;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.beans.ConstructorProperties;

/**
 * Data from the info.json query for a single image
 * Just store the height / width for our purpose.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class IIIFImageInfo {

    @JsonProperty("height")
    private int height;

    @JsonProperty("width")
    private int width;

    @ConstructorProperties({"height", "width"})
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public IIIFImageInfo(int height, int width) {
        this.width = width;
        this.height  = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
