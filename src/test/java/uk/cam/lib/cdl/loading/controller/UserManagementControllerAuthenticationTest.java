package uk.cam.lib.cdl.loading.controller;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import uk.cam.lib.cdl.loading.UserManagementController;
import uk.cam.lib.cdl.loading.apis.EditAPI;
import uk.cam.lib.cdl.loading.apis.MockGitRepo;
import uk.cam.lib.cdl.loading.config.GitLocalVariables;
import uk.cam.lib.cdl.loading.dao.UserRepository;
import uk.cam.lib.cdl.loading.dao.WorkspaceRepository;
import uk.cam.lib.cdl.loading.exceptions.GitHelperException;
import uk.cam.lib.cdl.loading.model.editor.Workspace;
import uk.cam.lib.cdl.loading.utils.GitHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringJUnitConfig
@WebMvcTest(controllers = UserManagementController.class)
class UserManagementControllerAuthenticationTest {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void setup() {

        Workspace workspace = new Workspace();
        workspace.setId(1);
        workspace.setName("Workspace One");
        workspace.setCollectionIds(ImmutableList.of("collections/test.collection.json"));
        List<Workspace> workspaces = new ArrayList<>();
        workspaces.add(workspace);

        when(workspaceRepository.findWorkspaceById(1)).thenReturn(workspace);
        when(workspaceRepository.findWorkspaceById(2)).thenReturn(null);
        when(workspaceRepository.findWorkspaceByCollectionIds("collections/test.collection.json")).thenReturn(workspaces);

        mvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    void UnauthorisedUserManagementScreen_shouldFailWith401() throws Exception {

        mvc.perform(get("/user-management/"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());

    }

    @WithMockUser(username="test-site-manager", roles = {"SITE_MANAGER"})
    @Test
    void givenAuthRequestOnUserManagementScreenSiteManager_shouldSucceedWith200() throws Exception {
        mvc.perform(get("/user-management/"))
            .andExpect(MockMvcResultMatchers.status().isOk());

    }

    @WithMockUser(username="test-workspace-manager1", roles = {"WORKSPACE_MANAGER1"})
    @Test
    void givenAuthRequestOnUserManagementScreenWorkspaceManager_shouldSucceedWith200() throws Exception {
        mvc.perform(get("/user-management/"))
            .andExpect(MockMvcResultMatchers.status().isOk());

    }

    @WithMockUser(username="test-workspace-member1", roles = {"WORKSPACE_MEMBER1"})
    @Test
    void givenAuthRequestOnUserManagementScreen_shouldFailWith403() throws Exception {
        mvc.perform(get("/user-management/"))
            .andExpect(MockMvcResultMatchers.status().isForbidden());

    }

    @Test
    void UnauthorisedEditUser_shouldFailWith401() throws Exception {
        mvc.perform(get("/user-management/user/edit"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser(username="test-workspace-member1", roles = {"WORKSPACE_MEMBER1"})
    void AuthorisedEditUser_shouldFailWith403() throws Exception {
        mvc.perform(get("/user-management/user/edit"))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username="test-site-manager", roles = {"SITE_MANAGER"})
    void AuthorisedEditUserSiteManager_shouldSucceedWith200() throws Exception {
        mvc.perform(get("/user-management/user/edit"))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(username="test-workspace-manager1", roles = {"WORKSPACE_MANAGER1"})
    void AuthorisedEditUserWorkspaceManager_shouldSucceedWith200() throws Exception {
        mvc.perform(get("/user-management/user/edit"))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    // TODO tests for setting user roles

    @Test
    void updateUserFromForm() {
        // TODO
    }

    @Test
    void deleteUser() {
        // TODO
    }

    @Test
    void UnauthorisedEditWorkspace_shouldFailWith401() throws Exception {
        mvc.perform(get("/user-management/workspace/edit?id=1"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser(username="test-workspace-manager1", roles = {"WORKSPACE_MANAGER1"})
    void AuthorisedWorkspaceManagerEditWorkspaceWithId_shouldSucceedWith200() throws Exception {
        mvc.perform(get("/user-management/workspace/edit?id=1"))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(username="test-workspace-manager2", roles = {"WORKSPACE_MANAGER2"})
    void AuthorisedWorkspaceManagerEditWorkspaceWithId_shouldFailWith403() throws Exception {
        mvc.perform(get("/user-management/workspace/edit?id=1"))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username="test-workspace-manager1", roles = {"WORKSPACE_MANAGER1"})
    void AuthorisedWorkspaceManagerEditNewWorkspaceWithId_shouldFailWith403() throws Exception {
        mvc.perform(get("/user-management/workspace/edit"))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username="test-site-manager", roles = {"SITE_MANAGER"})
    void AuthorisedSiteManagerEditNewWorkspaceWithId_shouldSucceedWith200() throws Exception {
        mvc.perform(get("/user-management/workspace/edit"))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(username="test-site-manager", roles = {"SITE_MANAGER"})
    void AuthorisedSiteManagerEditWorkspaceWithId_shouldSucceedWith200() throws Exception {
        mvc.perform(get("/user-management/workspace/edit?id=1"))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void UnauthorisedUpdateWorkspaceFromForm_shouldFailWith401() throws Exception {
        mvc.perform(post("/user-management/workspace/update")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("id", "1")
            .param("name", "Workspace One")
            .param("collectionIds", "test/test-collection.json"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser(username="test-site-manager", roles = {"SITE_MANAGER"})
    void AuthorisedSiteManagerUpdateWorkspaceFromForm_shouldSucceedWith302() throws Exception {
        mvc.perform(post("/user-management/workspace/update")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("id", "1")
            .param("name", "Workspace One")
            .param("collectionIds", "test/test-collection.json"))
            .andExpect(MockMvcResultMatchers.status().isFound());
    }

    @Test
    @WithMockUser(username="test-workspace-manager1", roles = {"WORKSPACE_MANAGER1"})
    void AuthorisedWorkspaceManagerUpdateWorkspaceFromForm_shouldSucceedWith302() throws Exception {
        mvc.perform(post("/user-management/workspace/update")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("id", "1")
            .param("name", "Workspace One")
            .param("collectionIds", "test/test-collection.json"))
            .andExpect(MockMvcResultMatchers.status().isFound());
    }

    @Test
    @WithMockUser(username="test-workspace-manager2", roles = {"WORKSPACE_MANAGER2"})
    void AuthorisedWorkspaceManagerUpdateWorkspaceFromForm_shouldFailWith403() throws Exception {
        mvc.perform(post("/user-management/workspace/update")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("id", "1")
            .param("name", "Workspace One")
            .param("collectionIds", "test/test-collection.json"))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    void UnauthorisedUpdateNewWorkspaceFromForm_shouldFailWith401() throws Exception {
        mvc.perform(post("/user-management/workspace/update")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("id", "2")
            .param("name", "Workspace Two")
            .param("collectionIds", "test/test-collection.json"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser(username="test-workspace-manager2", roles = {"WORKSPACE_MANAGER2"})
    void AuthorisedUpdateNewWorkspaceFromForm_shouldFailWith403() throws Exception {
        mvc.perform(post("/user-management/workspace/update")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("id", "2")
            .param("name", "Workspace Two")
            .param("collectionIds", "test/test-collection.json"))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username="test-site-manager", roles = {"SITE_MANAGER"})
    void AuthorisedUpdateNewWorkspaceFromForm_shouldSucceedWith302() throws Exception {
        mvc.perform(post("/user-management/workspace/update")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("id", "2")
            .param("name", "Workspace Two")
            .param("collectionIds", "test/test-collection.json"))
            .andExpect(MockMvcResultMatchers.status().isFound());
    }

    @Test
    void unauthorisedDeleteWorkspace_shouldFailWith401() throws Exception {
        mvc.perform(post("/user-management/workspace/delete?id=1"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser(username="test-workspace-manager1", roles = {"WORKSPACE_MANAGER1"})
    void AuthorisedDeleteWorkspace_shouldFailWith403() throws Exception {
        mvc.perform(post("/user-management/workspace/delete?id=1"))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username="test-site-manager", roles = {"SITE_MANAGER"})
    void AuthorisedSiteManagerDeleteWorkspace_shouldSucceedWith302() throws Exception {
        mvc.perform(post("/user-management/workspace/delete?id=1"))
            .andExpect(MockMvcResultMatchers.status().isFound());
    }

    // Bare repo represents a mock version of the remote repo and it is cloned locally for testing.
    // content is added from the resources source-data dir.
    @Configuration
    @ComponentScan(basePackages = "uk.cam.lib.cdl.loading")
    static class Config {

        private GitLocalVariables gitLocalVariables;
        private String dataPath;
        private GitHelper gitHelper;

        public Config() throws IOException, GitAPIException, GitHelperException {
            MockGitRepo gitRepo = new MockGitRepo();
            Git git = gitRepo.getGit();

            // Create a new file
            File testSourceDir = new File("./src/test/resources/source-data");
            FileUtils.copyDirectory(testSourceDir, gitRepo.getCloneDir());

            // Commit the new file
            git.add().addFilepattern(".").call();
            git.commit().setMessage("Adding Test Data").setAuthor("testuser", "test@example.com ").call();

            dataPath = gitRepo.getCloneDir().getCanonicalPath() + "/data/";
            gitLocalVariables = new GitLocalVariables(gitRepo.getCloneDir().getCanonicalPath(), "data",
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
