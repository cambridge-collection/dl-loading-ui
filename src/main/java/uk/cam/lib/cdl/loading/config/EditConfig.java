package uk.cam.lib.cdl.loading.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import uk.cam.lib.cdl.loading.apis.EditAPI;

import java.io.IOException;

@ConditionalOnProperty(
    value = "edit.scheduling.enable", havingValue = "true", matchIfMissing = true
)
@Configuration
@EnableScheduling
public class EditConfig {

    private final EditAPI editAPI;

    @Autowired
    public EditConfig(@Value("${data.dl-dataset.filename}") String dlDatasetFilename,
                      @Value("${data.ui.filename}") String dlUIFilename,
                      GitLocalVariables gitSourceVariables,
                      @Value("${data.item.path}") String dataItemPath) {

        this.editAPI =
            new EditAPI(gitSourceVariables.getGitSourcePath() + gitSourceVariables.getGitSourceDataSubpath(),
                dlDatasetFilename, dlUIFilename,
                gitSourceVariables.getGitSourcePath() + dataItemPath, gitSourceVariables);

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

