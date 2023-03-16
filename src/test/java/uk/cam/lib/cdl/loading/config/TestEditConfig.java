package uk.cam.lib.cdl.loading.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.cam.lib.cdl.loading.apis.EditAPI;
import uk.cam.lib.cdl.loading.model.editor.UI;
import uk.cam.lib.cdl.loading.model.editor.ui.UIThemeData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Configuration
public class TestEditConfig {

    @Bean
    public EditAPI editAPI() {
        var editApi = Mockito.mock(EditAPI.class);
        Mockito.when(editApi.getDataLocalPath()).thenReturn(Path.of("/example/data/dir"));
        return editApi;
    }

    @Bean
    public UIThemeData uiThemeData() throws IOException {

        // Setup for reading json5
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
        mapper.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        mapper.enable(JsonReadFeature.ALLOW_TRAILING_COMMA.mappedFeature());
        mapper.enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES.mappedFeature());
        mapper.enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER.mappedFeature());
        mapper.enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS.mappedFeature());
        mapper.enable(JsonReadFeature.ALLOW_JAVA_COMMENTS.mappedFeature());
        mapper.enable(JsonReadFeature.ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS.mappedFeature());

        File testUI = new File("./src/test/resources/source-data/data/test.ui.json5");
        UI ui = mapper.readValue(testUI, UI.class);
        return ui.getThemeData();

    }
}
