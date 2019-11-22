package uk.cam.lib.cdl.loading.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GitConfig implements WebMvcConfigurer {

    @Bean
    public GitSourceVariables getSourceVariables(@Value("${git.sourcedata.checkout.path}") String gitSourcePath,
                                                 @Value("${git.sourcedata.checkout.subpath.data}") String gitSourceDataSubpath,
                                                 @Value("${git.sourcedata.url}") String gitSourceURL,
                                                 @Value("${git.sourcedata.url.username}") String gitSourceURLUserame,
                                                 @Value("${git.sourcedata.url.password}") String gitSourceURLPassword,
                                                 @Value("${git.sourcedata.branch}") String gitBranch,
                                                 @Value("${git.sourcedata.dl-dataset.filename}") String dlDatasetFilename
    ) {
        return new GitSourceVariables(gitSourcePath, gitSourceDataSubpath, gitSourceURL,
            gitSourceURLUserame,
            gitSourceURLPassword, gitBranch, dlDatasetFilename);

    }

}
