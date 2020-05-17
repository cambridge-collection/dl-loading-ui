package uk.cam.lib.cdl.loading.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.cam.lib.cdl.loading.utils.GitHelper;

import java.net.URL;

@Configuration
public class GitConfig implements WebMvcConfigurer {

    @Bean
    public GitLocalVariables getLocalSourceVariables(@Value("${git.sourcedata.checkout.path}") String gitSourcePath,
                                                     @Value("${git.sourcedata.checkout.subpath.data}") String gitSourceDataSubpath,
                                                     @Value("${git.sourcedata.url}") String gitSourceURL,
                                                     @Value("${git.sourcedata.url.username}") String gitSourceURLUserame,
                                                     @Value("${git.sourcedata.url.password}") String gitSourceURLPassword,
                                                     @Value("${git.sourcedata.branch}") String gitBranch
    ) {
        return new GitLocalVariables(gitSourcePath, gitSourceDataSubpath, gitSourceURL,
            gitSourceURLUserame,
            gitSourceURLPassword, gitBranch);

    }

    @Bean
    public GitAPIVariables getSourceAPIVariables(@Value("${git.api.url}") URL gitAPIURL,
                                                 @Value("${git.sourcedata.branch}") String gitBranch,
                                                 @Value("${git.api.url.part.tags}") String tagsURL,
                                                 @Value("${git.sourcedata.api.url.part.repo}") String repoURL,
                                                 @Value("${git.api.url.part.pipelines}") String pipelinesURL,
                                                 @Value("${git.api.username}") String gitUsername,
                                                 @Value("${git.api.password}") String gitPassword) {
        return new GitAPIVariables(gitAPIURL, gitBranch, tagsURL, repoURL, pipelinesURL, gitUsername, gitPassword);
    }

    @Bean
    @Profile("!test")
    public GitHelper gitHelper(GitLocalVariables repoInfo) {
        return new GitHelper(repoInfo);
    }
}
