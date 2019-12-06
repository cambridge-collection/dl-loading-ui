package uk.cam.lib.cdl.loading.model.editor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.beans.ConstructorProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UI {

    private final String type;
    private final String themeName;
    private final UIThemeData themeData;

    @ConstructorProperties({"type", "theme-name", "theme-data"})
    public UI(String type, String themeName, UIThemeData themeData) {
        this.type = type;
        this.themeName = themeName;
        this.themeData = themeData;
    }

    @JsonProperty("@type")
    public String getType() {
        return type;
    }

    public String getThemeName() {
        return themeName;
    }

    public UIThemeData getThemeData() {
        return themeData;
    }
}
