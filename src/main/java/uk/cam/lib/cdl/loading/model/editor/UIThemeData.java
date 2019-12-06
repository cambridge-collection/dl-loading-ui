package uk.cam.lib.cdl.loading.model.editor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.beans.ConstructorProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UIThemeData {

    private final List<UICollection> collections;

    @ConstructorProperties({"collections"})
    public UIThemeData(List<UICollection> collections) {
        this.collections = collections;
    }

    public List<UICollection> getCollections() {
        return collections;
    }
}
