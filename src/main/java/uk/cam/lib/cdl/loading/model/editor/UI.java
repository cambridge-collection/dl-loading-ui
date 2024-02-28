package uk.cam.lib.cdl.loading.model.editor;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.cam.lib.cdl.loading.model.editor.ui.UIThemeData;
import uk.cam.lib.cdl.loading.model.editor.ui.UIThemeUI;

import java.beans.ConstructorProperties;

public class UI {

    private final String type;
    private final String themeName;
    private final UIThemeData themeData;
    private final UIThemeUI themeUI;

    @ConstructorProperties({"type", "theme-name", "theme-data", "theme-ui"})
    public UI(String type, String themeName, UIThemeData themeData, UIThemeUI themeUI) {
        this.type = type;
        this.themeName = themeName;
        this.themeData = themeData;
        this.themeUI = themeUI;
    }

    @JsonProperty("@type")
    public String getType() {
        return type;
    }

    @JsonProperty("theme-name")
    public String getThemeName() {
        return themeName;
    }

    @JsonProperty("theme-data")
    public UIThemeData getThemeData() {
        return themeData;
    }

    @JsonProperty("theme-ui")
    public UIThemeUI getThemeUI() { return themeUI; }
}
