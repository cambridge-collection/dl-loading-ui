package uk.cam.lib.cdl.loading.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import uk.cam.lib.cdl.loading.apis.DeploymentAPI;
import uk.cam.lib.cdl.loading.apis.PackagingAPI;

import java.net.MalformedURLException;
import java.net.URL;

@Configuration
@EnableScheduling
public class APIConfig {

    private final DeploymentAPI deploymentAPI;
    private final PackagingAPI packagingAPI;

    public APIConfig(@Value("${deployment.api.url}") URL deploymentURL,
                     GitAPIVariables gitAPIVariables,
                     GitLocalVariables gitSourceVariables
    ) {
        this.deploymentAPI = new DeploymentAPI(deploymentURL);
        this.packagingAPI = new PackagingAPI(gitSourceVariables, gitAPIVariables);
    }

    @Bean
    public DeploymentAPI deploymentAPI() {
        return deploymentAPI;
    }

    @Bean
    public PackagingAPI packagingAPI() {
        return packagingAPI;
    }

}
