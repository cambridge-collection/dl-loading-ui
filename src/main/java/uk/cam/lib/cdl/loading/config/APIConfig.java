package uk.cam.lib.cdl.loading.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import uk.cam.lib.cdl.loading.apis.BitbucketAPI;
import uk.cam.lib.cdl.loading.apis.DeploymentAPI;

import java.net.MalformedURLException;
import java.net.URL;

@Configuration
@EnableScheduling
public class APIConfig {

    private final DeploymentAPI deploymentAPI;
    private final BitbucketAPI bbAPI;

    public APIConfig(@Value("${deployment.api.url}") URL deploymentURL,
                     @Value("${git.bitbucket.api.url}") URL gitURL,
                     @Value("${git.bitbucket.api.url.part.tags}") String tagsURL,
                     @Value("${git.bitbucket.api.username}") String gitUsername,
                     @Value("${git.bitbucket.api.password}") String gitPassword
    ) throws MalformedURLException {
        this.deploymentAPI = new DeploymentAPI(deploymentURL);
        this.bbAPI = new BitbucketAPI(gitURL, tagsURL, gitUsername, gitPassword);
    }


    @Bean
    public DeploymentAPI deploymentAPI() {
        return deploymentAPI;
    }

    @Bean
    public BitbucketAPI bitbucketAPI() {
        return bbAPI;
    }

}
