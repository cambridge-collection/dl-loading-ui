package uk.cam.lib.cdl.loading.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import uk.cam.lib.cdl.loading.apis.BitBucketAPI;
import uk.cam.lib.cdl.loading.apis.DeploymentAPI;
import uk.cam.lib.cdl.loading.apis.PackagingAPI;

import java.net.MalformedURLException;
import java.net.URL;

@Configuration
@EnableScheduling
public class APIConfig {

    private final DeploymentAPI deploymentAPI;
    private final BitBucketAPI releaseBBAPI;
    private final BitBucketAPI sourceBBAPI;
    private final PackagingAPI packagingAPI;

    public APIConfig(@Value("${deployment.api.url}") URL deploymentURL,
                     @Value("${git.api.url}") URL gitAPIURL,
                     @Value("${git.sourcedata.branch}") String gitSourceBranch,
                     @Value("${git.releasedata.branch}") String gitReleaseBranch,
                     @Value("${git.api.url.part.tags}") String tagsURL,
                     @Value("${git.releasedata.api.url.part.repo}") String releaseRepoURL,
                     @Value("${git.sourcedata.api.url.part.repo}") String sourceRepoURL,
                     @Value("${git.api.url.part.pipelines}") String pipelinesURL,
                     @Value("${git.api.username}") String gitUsername,
                     @Value("${git.api.password}") String gitPassword,
                     GitSourceVariables gitSourceVariables
    ) throws MalformedURLException {
        this.deploymentAPI = new DeploymentAPI(deploymentURL);
        this.releaseBBAPI = new BitBucketAPI(gitAPIURL, gitReleaseBranch, releaseRepoURL, tagsURL, pipelinesURL,
            gitUsername,
            gitPassword);
        this.sourceBBAPI = new BitBucketAPI(gitAPIURL, gitSourceBranch, sourceRepoURL, tagsURL, pipelinesURL,
            gitUsername,
            gitPassword);
        this.packagingAPI = new PackagingAPI(gitSourceVariables, sourceBBAPI);
    }


    @Bean
    public DeploymentAPI deploymentAPI() {
        return deploymentAPI;
    }

    @Bean("releaseRepo")
    public BitBucketAPI releaseAPI() {
        return releaseBBAPI;
    }


    @Bean("sourceRepo")
    public BitBucketAPI sourceAPI() {
        return releaseBBAPI;
    }


    @Bean
    public PackagingAPI packagingAPI() {
        return packagingAPI;
    }

}
