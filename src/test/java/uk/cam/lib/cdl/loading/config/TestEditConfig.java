package uk.cam.lib.cdl.loading.config;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import uk.cam.lib.cdl.loading.apis.EditAPI;

@Configuration
public class TestEditConfig {
    @Bean
    public EditAPI editAPI() {
        return Mockito.mock(EditAPI.class);
    }
}
