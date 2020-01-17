package uk.cam.lib.cdl.loading.apis;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.cam.lib.cdl.loading.config.EditConfig;
import uk.cam.lib.cdl.loading.config.GitAPIVariables;
import uk.cam.lib.cdl.loading.config.GitConfig;
import uk.cam.lib.cdl.loading.config.GitLocalVariables;
import uk.cam.lib.cdl.loading.model.packaging.PackagingStatus;
import uk.cam.lib.cdl.loading.model.packaging.Pipeline;

import java.io.IOException;
import java.net.URL;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {EditConfig.class, GitConfig.class})
@AutoConfigureWireMock(port = 8089)
class PackagingAPITest {

    private PackagingAPI packagingAPI;
    private GitLocalVariables gitSourceVariables;
    private GitAPIVariables gitAPIVariables;

    public PackagingAPITest() throws IOException, GitAPIException {

        MockGitRepo gitRepo = new MockGitRepo();

        gitSourceVariables = new GitLocalVariables(gitRepo.getCloneDir().getCanonicalPath(), "/data",
            "gitSourceURL", "gitSourceURLUserame",
            "gitSourceURLPassword", "gitBranch");


        String gitAPIURL = "http://localhost:8089/git/api";

        gitAPIVariables = new GitAPIVariables(new URL(gitAPIURL),
            "gitBranch",
            "/tags",
            "/pipelines",
            "/pipelines",
            "gitUsername",
            "gitPassword");

            packagingAPI = new PackagingAPI(gitSourceVariables, gitAPIVariables);

    }

    @Test
    void getHistory() {
        final List<Pipeline> history = packagingAPI.getHistory();
        assert (history.size()==10);
        assert (history.get(0).getId().equals("{33ced4f9-0055-48e2-9e92-8ba73b6c52d0}"));
        assert (history.get(0).getBuildNumber()==171);
        assert (history.get(0).getStatus().getResultType().equals("pipeline_state_completed_successful"));
        assert (history.get(0).getCreated().toString().equals( "Wed Dec 11 11:29:16 GMT 2019"));
        assert (history.get(0).getCompleted().toString().equals( "Wed Dec 11 11:32:52 GMT 2019"));
    }

    @Test
    void getTags() {
        // TODO
    }

    @Test
    void updatesSinceLastPackage() {
        // TODO
    }

    @Test
    void startProcess() {
        // TODO
    }

    @Test
    void getStatus() {
        final PackagingStatus status = packagingAPI.getStatus("testid");
        assert (status.getName().equals("COMPLETED"));
        assert (status.getResultName().equals("SUCCESSFUL"));
        assert (status.getType().equals("pipeline_state_completed"));
        assert (status.getResultType().equals("pipeline_state_completed_successful"));
    }
}