package uk.cam.lib.cdl.loading.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import uk.cam.lib.cdl.loading.apis.EditAPI;
import uk.cam.lib.cdl.loading.apis.EditAPIUpdater;
import uk.cam.lib.cdl.loading.utils.GitHelper;

import java.nio.file.Path;

@Configuration
@EnableScheduling
public class EditConfig {
    @Bean
    public Path dlDatasetFilename(@Value("${data.dl-dataset.filename}") Path dlDatasetFilename) {
        return dlDatasetFilename;
    }

    @Bean
    public Path dlUIFilename(@Value("${data.ui.filename}") Path dlUIFilename) {
        return dlUIFilename;
    }

    @Bean Path dataItemPath(@Value("${data.item.path}") Path dataItemPath) {
        if(dataItemPath.isAbsolute()) {
            throw new IllegalArgumentException(String.format("dataItemPath cannot be absolute: '%s'", dataItemPath));
        }
        return dataItemPath;
    }

    @Bean
    public EditAPI editAPI(GitHelper gitHelper, GitLocalVariables gitSourceVariables, Path dlDatasetFilename, Path dlUIFilename, Path dataItemPath) {
        return new EditAPI(Path.of(gitSourceVariables.getGitSourcePath(), gitSourceVariables.getGitSourceDataSubpath()).toString(),
            dlDatasetFilename.toString(), dlUIFilename.toString(),
            Path.of(gitSourceVariables.getGitSourcePath()).resolve(dataItemPath).toString(),
            gitHelper);
    }

    @ConditionalOnProperty(
        value = "edit.scheduling.enable", havingValue = "true", matchIfMissing = true
    )
    @Bean
    public EditAPIUpdater editAPIUpdater(EditAPI editAPI) {
        return new EditAPIUpdater(editAPI);
    }
}

