package uk.cam.lib.cdl.loading.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.cam.lib.cdl.loading.apis.BitbucketAPI;
import uk.cam.lib.cdl.loading.apis.DeploymentAPI;

import java.net.MalformedURLException;
import java.net.URL;

@Configuration
public class APIConfig {

    @Value("${deployment.api.url}")
    URL deploymentURL;

    @Value("${git.bitbucket.api.url}")
    URL gitURL;

    @Value("${git.bitbucket.api.url.part.tags}")
    String tagsURL;

    @Value("${git.bitbucket.api.username}")
    String gitUsername;

    @Value("${git.bitbucket.api.password}")
    String gitPassword;

    @Bean
    public DeploymentAPI deploymentAPI() {
        DeploymentAPI dao = new DeploymentAPI(deploymentURL);
        return dao;
    }

    @Bean
    public BitbucketAPI bitbucketAPI() throws MalformedURLException {
        BitbucketAPI bb = new BitbucketAPI(gitURL, tagsURL, gitUsername, gitPassword);
        return bb;
    }


}
