package uk.cam.lib.cdl.loading.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.cam.lib.cdl.loading.apis.EditAPI;
import uk.cam.lib.cdl.loading.model.editor.Id;

import java.io.IOException;

@Configuration
@EnableScheduling
public class EditConfig {

    private final GitVariables gitVariables;
    private final EditAPI editAPI;

    @Autowired
    public EditConfig(@Value("${git.sourcedata.checkout.path}") String gitSourcePath,
                      @Value("${git.sourcedata.checkout.subpath.data}") String gitSourceDataSubpath,
                      @Value("${git.sourcedata.url}") String gitSourceURL,
                      @Value("${git.sourcedata.url.username}") String gitSourceURLUserame,
                      @Value("${git.sourcedata.url.password}") String gitSourceURLPassword,
                      @Value("${git.sourcedata.branch}") String gitBranch,
                      @Value("${git.sourcedata.dl-dataset.filename}") String dlDatasetFilename,
                      @Value("${data.item.path}") String dataItemPath
    ) {

        this.gitVariables = new GitVariables(gitSourcePath, gitSourceDataSubpath, gitSourceURL, gitSourceURLUserame,
                gitSourceURLPassword, gitBranch, dlDatasetFilename);

        this.editAPI = new EditAPI(gitSourcePath + gitSourceDataSubpath, dlDatasetFilename, gitSourcePath + dataItemPath);

    }


    @Bean
    public GitVariables gitVariables() {
        return gitVariables;
    }

    @Scheduled(fixedDelay = 5 * 60 * 1000, initialDelay = 500) // Every 5 mins
    public void checkForUpdates() throws IOException {
        System.out.println("Updating model...");
        editAPI.updateModel();
    }

    @Bean
    public EditAPI editAPI() {
        return editAPI;
    }

}

