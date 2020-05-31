package uk.cam.lib.cdl.loading.config;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.cam.lib.cdl.loading.utils.GitHelper;

@Configuration
public class TestGitConfig {
    @Bean
    public GitHelper gitHelper() {
        return Mockito.mock(GitHelper.class);
    }
}
