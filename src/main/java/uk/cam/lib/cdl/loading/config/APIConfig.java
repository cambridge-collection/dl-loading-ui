package uk.cam.lib.cdl.loading.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import uk.cam.lib.cdl.loading.apis.DeploymentAPI;
import uk.cam.lib.cdl.loading.apis.PackagingAPI;

import java.net.URL;

@Configuration
@EnableScheduling
public class APIConfig {
    @Bean
    public DeploymentAPI deploymentAPI(@Value("${deployment.api.url}") URL deploymentURL) {
        return new DeploymentAPI(deploymentURL);
    }

    @Bean
    public PackagingAPI packagingAPI(GitLocalVariables gitSourceVariables, GitAPIVariables gitAPIVariables) {
        return new PackagingAPI(gitSourceVariables, gitAPIVariables);
    }
}
