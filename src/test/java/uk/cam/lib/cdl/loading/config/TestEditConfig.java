package uk.cam.lib.cdl.loading.config;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.cam.lib.cdl.loading.apis.EditAPI;

import java.nio.file.Path;

@Configuration
public class TestEditConfig {

    @Bean
    public EditAPI editAPI() {
        var editApi = Mockito.mock(EditAPI.class);
        Mockito.when(editApi.getDataLocalPath()).thenReturn(Path.of("/example/data/dir"));
        return editApi;
    }
}
