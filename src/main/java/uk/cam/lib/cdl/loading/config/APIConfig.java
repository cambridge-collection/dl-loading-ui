package uk.cam.lib.cdl.loading.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.cam.lib.cdl.loading.apis.BitbucketAPI;
import uk.cam.lib.cdl.loading.apis.DeploymentAPI;
import uk.cam.lib.cdl.loading.apis.EditAPI;

import java.io.IOException;
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

    @Value("${git.sourcedata.dl-dataset.filename}")
    String dlDatasetFilename;

    @Value("${git.sourcedata.checkout.path}")
    String dataPathStart;

    @Value("${git.sourcedata.checkout.subpath.data}")
    String dataPathDir;

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

    @Bean
    public EditAPI editAPI() throws IOException {
        EditAPI edit = new EditAPI(dataPathStart + dataPathDir, dlDatasetFilename);
        return edit;
    }


}
