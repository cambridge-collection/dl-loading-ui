package uk.cam.lib.cdl.loading.config;

import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import uk.cam.lib.cdl.loading.apis.EditAPI;
import uk.cam.lib.cdl.loading.apis.EditAPIUpdater;
import uk.cam.lib.cdl.loading.exceptions.EditApiException;
import uk.cam.lib.cdl.loading.model.editor.ModelOps;
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

    @Bean Path absoluteDataItemPath(@Qualifier("dataRoot") Path dataRoot, @Qualifier("dataItemPath") Path dataItemPath) {
        Preconditions.checkArgument(dataRoot.isAbsolute());
        Preconditions.checkArgument(!dataItemPath.isAbsolute());
        var absoluteDataItemPath = dataRoot.resolve(dataItemPath).normalize();
        Preconditions.checkArgument(absoluteDataItemPath.startsWith(dataRoot));
        return absoluteDataItemPath;
    }

    @Bean Path dataRoot(@Value("${git.sourcedata.checkout.path}") Path gitRepoRoot,
                        @Value("${git.sourcedata.checkout.subpath.data}") Path gitRepoDataSubpath) {
        Preconditions.checkArgument(gitRepoRoot.isAbsolute());
        Preconditions.checkArgument(!gitRepoDataSubpath.isAbsolute());
        var dataRoot = gitRepoRoot.resolve(gitRepoDataSubpath).normalize();
        Preconditions.checkArgument(dataRoot.startsWith(gitRepoRoot));
        return dataRoot;
    }

    @Bean
    @Profile("!test")
    public EditAPI editAPI(Path dataRoot, GitHelper gitHelper, Path dlDatasetFilename,
                           Path dlUIFilename, Path absoluteDataItemPath) throws EditApiException {
        return new EditAPI(
            dataRoot.toString(),
            dlDatasetFilename.toString(),
            dlUIFilename.toString(),
            absoluteDataItemPath.toString(),
            gitHelper);
    }

    @ConditionalOnProperty(
        value = "edit.scheduling.enable", havingValue = "true", matchIfMissing = true
    )
    @Bean
    public EditAPIUpdater editAPIUpdater(EditAPI editAPI) {
        return new EditAPIUpdater(editAPI);
    }

    @Bean(name = "ModelOps")
    public ModelOps modelOps() {
        return ModelOps.ModelOps();
    }
}

