package uk.cam.lib.cdl.loading.config;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import uk.cam.lib.cdl.loading.apis.EditAPI;
import uk.cam.lib.cdl.loading.apis.MockGitRepo;
import uk.cam.lib.cdl.loading.exceptions.EditApiException;
import uk.cam.lib.cdl.loading.exceptions.GitHelperException;
import uk.cam.lib.cdl.loading.utils.GitHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

// Bare repo represents a mock version of the remote repo and it is cloned locally for testing.
// content is added from the resources source-data dir.
@TestConfiguration
public class GitRepoWithTestDataConfig {
    @Bean
    public MockGitRepo mockGitRepo() throws IOException, GitAPIException {
        MockGitRepo gitRepo = new MockGitRepo();
        Git git = gitRepo.getGit();

        // Create a new file
        File testSourceDir = new File("./src/test/resources/source-data");
        FileUtils.copyDirectory(testSourceDir, gitRepo.getCloneDir());

        // Commit the new file
        git.add().addFilepattern(".").call();
        git.commit().setMessage("Adding Test Data").setAuthor("testuser", "test@example.com ").call();

        return gitRepo;
    }

    @Bean
    public GitHelper gitHelper(MockGitRepo mockGitRepo, GitLocalVariables gitLocalVariables) throws GitHelperException {
        return new GitHelper(mockGitRepo.getGit(), gitLocalVariables);
    }

    @Bean
    public Path gitRoot(MockGitRepo mockGitRepo) {
        return mockGitRepo.getCloneDir().toPath();
    }

    @Bean
    public Path dataRoot(Path gitRoot, Path gitRepoDataSubpath) {
        return gitRoot.resolve(gitRepoDataSubpath);
    }

    @Bean
    public Path gitRepoDataSubpath() {
        return Path.of("data");
    }

    @Bean
    @Primary
    public GitLocalVariables gitLocalVariables(Path gitRoot, Path gitRepoDataSubpath) {
        return new GitLocalVariables(gitRoot.toString(), gitRepoDataSubpath.toString(),
            "gitSourceURL", "gitSourceURLUserame",
            "gitSourceURLPassword", "gitBranch");
    }

    @Bean
    public EditAPI editAPI(Path dataRoot, GitHelper gitHelper, Path dlDatasetFilename,
                           Path dlUIFilename, Path absoluteDataItemPath) throws EditApiException {
        return new EditAPI(
            dataRoot.toString(),
            dlDatasetFilename.toString(),
            dlUIFilename.toString(),
            absoluteDataItemPath.toString(),
            gitHelper);
    }
}
