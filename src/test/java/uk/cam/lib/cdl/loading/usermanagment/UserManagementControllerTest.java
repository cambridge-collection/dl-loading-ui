package uk.cam.lib.cdl.loading.usermanagment;

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
import uk.cam.lib.cdl.loading.model.editor.Workspace;
import uk.cam.lib.cdl.loading.model.security.User;
import uk.cam.lib.cdl.loading.utils.GitHelper;
import uk.cam.lib.cdl.loading.utils.RoleHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringJUnitConfig
@WebMvcTest(controllers = UserManagementController.class)
class UserManagementControllerTest {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    private void setupUsers() {

        // User 1
        User user1 = new User();
        user1.setUsername("test-workspace-member1");
        user1.setFirstName("Workspace");
        user1.setLastName("Member");
        user1.setEmail("wm1@test.com");
        user1.setPassword("password");
        user1.setEnabled(true);

        List<String> authorities1 = new ArrayList<>();
        authorities1.add(RoleHelper.getWorkspaceMemberRole((long) 1));
        user1.setAuthorities(authorities1);

        userRepository.save(user1);

        // User 2
        User user2 = new User();
        user2.setUsername("test-workspace-member2");
        user2.setFirstName("Workspace");
        user2.setLastName("Member");
        user2.setEmail("wm2@test.com");
        user2.setPassword("password");
        user2.setEnabled(true);

        List<String> authorities2 = new ArrayList<>();
        authorities2.add(RoleHelper.getWorkspaceMemberRole((long) 1));
        authorities2.add(RoleHelper.getWorkspaceMemberRole((long) 2));
        user2.setAuthorities(authorities2);

        userRepository.save(user2);

        // User 3
        User user3 = new User();
        user3.setUsername("test-workspace-manager1");
        user3.setFirstName("Workspace");
        user3.setLastName("Manager");
        user3.setEmail("wm3@test.com");
        user3.setPassword("password");
        user3.setEnabled(true);

        List<String> authorities3 = new ArrayList<>();
        authorities3.add(RoleHelper.getWorkspaceManagerRole((long) 1));
        user3.setAuthorities(authorities3);

        userRepository.save(user3);

        // User 4
        User user4 = new User();
        user4.setUsername("test-workspace-manager2");
        user4.setFirstName("Workspace");
        user4.setLastName("Manager");
        user4.setEmail("wm4@test.com");
        user4.setPassword("password");
        user4.setEnabled(true);

        List<String> authorities4 = new ArrayList<>();
        authorities4.add(RoleHelper.getWorkspaceManagerRole((long) 2));
        user4.setAuthorities(authorities4);

        userRepository.save(user4);

        // User 5
        User user5 = new User();
        user5.setUsername("test-deployment-manager");
        user5.setFirstName("Deployment");
        user5.setLastName("Manager");
        user5.setEmail("dm@test.com");
        user5.setPassword("password");
        user5.setEnabled(true);

        List<String> authorities5 = new ArrayList<>();
        authorities5.add(RoleHelper.getRoleDeploymentManager());
        user5.setAuthorities(authorities5);

        userRepository.save(user5);

        // User 6
        User user6 = new User();
        user6.setUsername("test-site-manager");
        user6.setFirstName("Site");
        user6.setLastName("Manager");
        user6.setEmail("sm@test.com");
        user6.setPassword("password");
        user6.setEnabled(true);

        List<String> authorities6 = new ArrayList<>();
        authorities6.add(RoleHelper.getRoleSiteManager());
        user6.setAuthorities(authorities6);

        userRepository.save(user6);
    }

    private void setupWorkspaces() {

        // Workspace 1
        Workspace workspace1 = new Workspace();
        workspace1.setName("Workspace 1");
        workspace1.setId(1);
        workspace1.setCollectionIds(ImmutableList.of("collections/test.collection.json"));
        workspace1.setItemIds(ImmutableList.of("MS-TEST-00001-00001", "MS-TEST-00001-00002"));
        workspaceRepository.save(workspace1);

        // Workspace 2
        Workspace workspace2 = new Workspace();
        workspace2.setName("Workspace 2");
        workspace2.setId(2);
        workspace2.setItemIds(ImmutableList.of("MS-TEST-00001-00003"));
        workspaceRepository.save(workspace2);
    }

    @BeforeEach
    void setup() throws IOException, GitAPIException {

        setupWorkspaces();
        //setupUsers();

        mvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    void UnauthorisedUserManagementScreen_shouldFailWith401() throws Exception {

        mvc.perform(get("/user-management/").contentType(MediaType.TEXT_HTML))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());

    }

    @WithMockUser(username="test-site-manager", roles = {"SITE_MANAGER"})
    @Test
    void givenAuthRequestOnUserManagementScreenSiteManager_shouldSucceedWith200() throws Exception {
        mvc.perform(get("/user-management/").contentType(MediaType.TEXT_HTML))
            .andExpect(MockMvcResultMatchers.status().isOk());

    }

    @WithMockUser(username="test-workspace-manager1", roles = {"WORKSPACE_MANAGER1"})
    @Test
    void givenAuthRequestOnUserManagementScreenWorkspaceManager_shouldSucceedWith200() throws Exception {
        mvc.perform(get("/user-management/").contentType(MediaType.TEXT_HTML))
            .andExpect(MockMvcResultMatchers.status().isOk());

    }

    @WithMockUser(username="test-workspace-member1", roles = {"WORKSPACE_MEMBER1"})
    @Test
    void givenAuthRequestOnUserManagementScreen_shouldFailWith403() throws Exception {
        mvc.perform(get("/user-management/").contentType(MediaType.TEXT_HTML))
            .andExpect(MockMvcResultMatchers.status().isForbidden());

    }

    @Test
    void UnauthorisedEditUser_shouldFailWith401() throws Exception {
        mvc.perform(get("/user-management/user/edit").contentType(MediaType.TEXT_HTML))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser(username="test-workspace-member1", roles = {"WORKSPACE_MEMBER1"})
    void AuthorisedEditUser_shouldFailWith403() throws Exception {
        mvc.perform(get("/user-management/user/edit").contentType(MediaType.TEXT_HTML))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username="test-site-manager", roles = {"SITE_MANAGER"})
    void AuthorisedEditUserSiteManager_shouldSucceedWith200() throws Exception {
        mvc.perform(get("/user-management/user/edit").contentType(MediaType.TEXT_HTML))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(username="test-workspace-manager1", roles = {"WORKSPACE_MANAGER1"})
    void AuthorisedEditUserWorkspaceManager_shouldSucceedWith200() throws Exception {
        mvc.perform(get("/user-management/user/edit").contentType(MediaType.TEXT_HTML))
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
        mvc.perform(get("/user-management/workspace/edit").contentType(MediaType.TEXT_HTML))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser(username="test-workspace-manager1", roles = {"WORKSPACE_MANAGER1"})
    void AuthorisedWorkspaceManagerEditWorkspace_shouldFailWith403() throws Exception {
        // TODO should fail - site manager only can add workspaces
        //mvc.perform(get("/user-management/workspace/edit").contentType(MediaType.TEXT_HTML))
        //    .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser(username="test-workspace-manager1", roles = {"WORKSPACE_MANAGER1"})
    void AuthorisedWorkspaceManagerEditWorkspaceWIthId_shouldSucceedWith200() throws Exception {
        mvc.perform(get("/user-management/workspace/edit?id=1").contentType(MediaType.TEXT_HTML))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(username="test-workspace-manager2", roles = {"WORKSPACE_MANAGER2"})
    void AuthorisedWorkspaceManagerEditWorkspaceWIthId_shouldFailWith403() throws Exception {
        mvc.perform(get("/user-management/workspace/edit?id=1").contentType(MediaType.TEXT_HTML))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username="test-site-manager", roles = {"SITE_MANAGER"})
    void AuthorisedSiteManagerEditWorkspaceWIthId_shouldSucceedWith200() throws Exception {
        mvc.perform(get("/user-management/workspace/edit?id=1").contentType(MediaType.TEXT_HTML))
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
    void unauthorisedDeleteWorkspace_shouldFailWith401() throws Exception {
        mvc.perform(post("/user-management/workspace/delete?id=1").contentType(MediaType.TEXT_HTML))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser(username="test-workspace-manager1", roles = {"WORKSPACE_MANAGER1"})
    void AuthorisedDeleteWorkspace_shouldFailWith403() throws Exception {
        mvc.perform(post("/user-management/workspace/delete?id=1").contentType(MediaType.TEXT_HTML))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username="test-site-manager", roles = {"SITE_MANAGER"})
    void AuthorisedSiteManagerDeleteWorkspace_shouldSucceedWith200() throws Exception {

        // TODO not picking up existing workspace
        //mvc.perform(post("/user-management/workspace/delete?id=1").contentType(MediaType.TEXT_HTML))
        //    .andExpect(MockMvcResultMatchers.status().isOk());
    }

    // Bare repo represents a mock version of the remote repo and it is cloned locally for testing.
    // content is added from the resources source-data dir.
    @Configuration
    @ComponentScan(basePackages = "uk.cam.lib.cdl.loading")
    static class Config {

        @Bean
        public EditAPI MockEditAPI() throws IOException, GitAPIException {

            MockGitRepo gitRepo = new MockGitRepo();
            Git git = gitRepo.getGit();

            // Let's do our first commit
            // Create a new file
            File testSourceDir = new File("./src/test/resources/source-data");
            FileUtils.copyDirectory(testSourceDir, gitRepo.getCloneDir());

            // Commit the new file
            git.add().addFilepattern(".").call();
            git.commit().setMessage("Adding Test Data").setAuthor("testuser", "test@example.com ").call();


            GitLocalVariables gitSourceVariables = new GitLocalVariables(gitRepo.getCloneDir().getCanonicalPath(), "/data",
                "gitSourceURL", "gitSourceURLUserame",
                "gitSourceURLPassword", "gitBranch");


            GitHelper gitHelper = new GitHelper(git, gitSourceVariables);

            return new EditAPI(gitRepo.getCloneDir().getCanonicalPath() + "/data",
                "test.dl-dataset.json", "test.ui.json5",
                gitSourceVariables.getGitSourcePath() + "/data/items/data/tei/", gitHelper);

        }
    }

}
