package uk.cam.lib.cdl.loading.controller;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.UriComponentsBuilder;
import uk.cam.lib.cdl.loading.DeployController;
import uk.cam.lib.cdl.loading.apis.DeploymentAPI;
import uk.cam.lib.cdl.loading.apis.EditAPI;
import uk.cam.lib.cdl.loading.apis.MockGitRepo;
import uk.cam.lib.cdl.loading.config.GitLocalVariables;
import uk.cam.lib.cdl.loading.dao.UserRepository;
import uk.cam.lib.cdl.loading.dao.WorkspaceRepository;
import uk.cam.lib.cdl.loading.utils.GitHelper;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringJUnitConfig
@WebMvcTest(controllers = DeployController.class)
@AutoConfigureWireMock(port = 0)
class DeployControllerAuthenticationTest {

    @Autowired
    private WireMockServer wireMockServer;

    @MockBean
    private DeploymentAPI deploymentAPI;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void setup() throws IOException, GitAPIException {

        URI apiURL = UriComponentsBuilder.fromHttpUrl(wireMockServer.baseUrl())
            .path("/api/deploy/v0.1/").build().toUri();
        this.deploymentAPI = new DeploymentAPI(apiURL.toURL());

        mvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    void UnauthorisedDeployScreen_shouldFailWith401() throws Exception {

        mvc.perform(get("/deploy/deploy.html").contentType(MediaType.TEXT_HTML))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());

    }

    @WithMockUser(username="test-deployment-manager", roles = {"DEPLOYMENT_MANAGER"})
    @Test
    void givenAuthRequestOnDeployScreenDeployManager_shouldSucceedWith200() throws Exception {
        mvc.perform(get("/deploy/deploy.html").contentType(MediaType.TEXT_HTML))
            .andExpect(MockMvcResultMatchers.status().isOk());

    }

    @Test
    void UnauthorisedRefreshCache_shouldFailWith401() throws Exception {
        mvc.perform(get("/deploy/cache/refresh").contentType(MediaType.TEXT_HTML))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser(username="test-workspace-member1", roles = {"WORKSPACE_MEMBER1"})
    void AuthorisedRefreshCache_shouldFailWith403() throws Exception {
        mvc.perform(get("/deploy/cache/refresh").contentType(MediaType.TEXT_HTML))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username="test-deployment-manager", roles = {"DEPLOYMENT_MANAGER"})
    void AuthorisedRefreshCache_shouldSucceedWith200() throws Exception {
        mvc.perform(get("/deploy/cache/refresh").contentType(MediaType.TEXT_HTML))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void UnauthorisedDeployInstance_shouldFailWith401() throws Exception {
        mvc.perform(post("/deploy/testid").contentType(MediaType.TEXT_HTML))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser(username="test-workspace-manager1", roles = {"WORKSPACE_MANAGER1"})
    void AuthorisedDeployInstance_shouldFailWith403() throws Exception {
        // TODO
        //mvc.perform(post("/deploy/testid").contentType(MediaType.TEXT_HTML))
        //    .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username="test-deployment-manager", roles = {"DEPLOYMENT_MANAGER"})
    void AuthorisedDeployInstance_shouldSucceedWith200() throws Exception {
        // TODO
        //mvc.perform(post("/deploy/testid").contentType(MediaType.TEXT_HTML))
        //    .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void UnauthorisedDeployStatus_shouldFailWith401() throws Exception {
        mvc.perform(get("/deploy/status/testid").contentType(MediaType.TEXT_HTML))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser(username="test-workspace-manager1", roles = {"WORKSPACE_MANAGER1"})
    void AuthorisedDeployStatus_shouldFailWith403() throws Exception {
        mvc.perform(get("/deploy/status/testid").contentType(MediaType.TEXT_HTML))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username="test-deployment-manager", roles = {"DEPLOYMENT_MANAGER"})
    void AuthorisedDeployStatus_shouldSucceedWith200() throws Exception {
        // TODO
        //mvc.perform(get("/deploy/status/testid").contentType(MediaType.TEXT_HTML))
        //    .andExpect(MockMvcResultMatchers.status().isOk());
    }

    // Bare repo represents a mock version of the remote repo and it is cloned locally for testing.
    // content is added from the resources source-data dir.
    @Configuration
    @ComponentScan(basePackages = "uk.cam.lib.cdl.loading")
    static class Config {

        private GitLocalVariables gitLocalVariables;
        private String dataPath;
        private GitHelper gitHelper;

        public Config() throws IOException, GitAPIException {
            MockGitRepo gitRepo = new MockGitRepo();
            Git git = gitRepo.getGit();

            // Create a new file
            File testSourceDir = new File("./src/test/resources/source-data");
            FileUtils.copyDirectory(testSourceDir, gitRepo.getCloneDir());

            // Commit the new file
            git.add().addFilepattern(".").call();
            git.commit().setMessage("Adding Test Data").setAuthor("testuser", "test@example.com ").call();

            dataPath = gitRepo.getCloneDir().getCanonicalPath() + "/data/";
            gitLocalVariables = new GitLocalVariables(gitRepo.getCloneDir().getCanonicalPath(), "/data/",
                "gitSourceURL", "gitSourceURLUserame",
                "gitSourceURLPassword", "gitBranch");

            gitHelper = new GitHelper(git, gitLocalVariables);

        }

        @Bean
        @Primary
        public GitLocalVariables getGitLocalVariables() {
            return gitLocalVariables;
        }

        @Bean
        public Path dlDatasetFilename() {
            return new File(dataPath+"test.dl-dataset.json").toPath();
        }

        @Bean
        public Path dlUIFilename() {
            return new File(dataPath+"test.ui.json5").toPath();
        }

        @Bean
        public Path dataItemPath() {
            return new File(dataPath+"data/items/data/tei/").toPath();
        }

        @Bean
        @Primary
        public EditAPI editAPI(GitLocalVariables gitLocalVariables, Path dlDatasetFilename, Path dlUIFilename, Path dataItemPath) {
            return new EditAPI(dataPath,
                "test.dl-dataset.json", "test.ui.json5",
                gitLocalVariables.getGitSourcePath() + "/data/items/data/tei/", gitHelper);

        }
    }

}
