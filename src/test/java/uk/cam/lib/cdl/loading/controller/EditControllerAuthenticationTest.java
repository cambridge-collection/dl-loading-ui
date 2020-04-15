package uk.cam.lib.cdl.loading.controller;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.cam.lib.cdl.loading.EditController;
import uk.cam.lib.cdl.loading.apis.EditAPI;
import uk.cam.lib.cdl.loading.apis.MockGitRepo;
import uk.cam.lib.cdl.loading.config.GitLocalVariables;
import uk.cam.lib.cdl.loading.dao.UserRepository;
import uk.cam.lib.cdl.loading.dao.WorkspaceRepository;
import uk.cam.lib.cdl.loading.model.editor.Workspace;
import uk.cam.lib.cdl.loading.utils.GitHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringJUnitConfig
@WebMvcTest(controllers = EditController.class)
class EditControllerAuthenticationTest {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @Value("${data.url.display}")
    private String pathForDataDisplay;

    @BeforeEach
    void setup() {

        Workspace workspace = new Workspace();
        workspace.setId(1);
        workspace.setName("Workspace One");
        List<String> collectionIds = new ArrayList<>();
        collectionIds.add("collections/test.collection.json");
        workspace.setCollectionIds(collectionIds);
        List<Workspace> workspaces = new ArrayList<>();
        workspaces.add(workspace);

        when(workspaceRepository.findWorkspaceById(1)).thenReturn(workspace);
        when(workspaceRepository.findWorkspaceByCollectionIds("collections/test.collection.json")).thenReturn(workspaces);
        when(workspaceRepository.findWorkspaceByCollectionIds("collections/new.collection.json")).thenReturn(new ArrayList<>());
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(workspace);

        mvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    void UnauthorisedEditScreen_shouldFailWith401() throws Exception {
        mvc.perform(get("/edit/edit.html").contentType(MediaType.TEXT_HTML))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @WithMockUser(username="test-deployment-manager", roles = {"DEPLOYMENT_MANAGER"})
    @Test
    void givenAuthRequestOnEditScreenDeployManager_shouldFailWIth403() throws Exception {
        mvc.perform(get("/edit/edit.html").contentType(MediaType.TEXT_HTML))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @WithMockUser(username="test-workspace-manager1", roles = {"WORKSPACE_MANAGER1"})
    @Test
    void givenAuthRequestOnEditScreenWorkspaceManager_shouldSucceedWith200() throws Exception {
        mvc.perform(get("/edit/edit.html").contentType(MediaType.TEXT_HTML))
            .andExpect(MockMvcResultMatchers.status().isOk());

    }

    @WithMockUser(username="test-workspace-member1", roles = {"WORKSPACE_MEMBER1"})
    @Test
    void givenAuthRequestOnEditScreenWorkspaceMember_shouldSucceedWith200() throws Exception {
        mvc.perform(get("/edit/edit.html").contentType(MediaType.TEXT_HTML))
            .andExpect(MockMvcResultMatchers.status().isOk());

    }

    @Test
    void UnauthorisedEditNewCollection_shouldFailWith401() throws Exception {
        mvc.perform(get("/edit/collection/?workspaceIds=1").contentType(MediaType.TEXT_HTML))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser(username="test-deployment-manager", roles = {"DEPLOYMENT_MANAGER"})
    void AuthorisedEditNewCollection_shouldFailWith403() throws Exception {
        mvc.perform(get("/edit/collection/?workspaceIds=1").contentType(MediaType.TEXT_HTML))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username="test-workspace-member1", roles = {"WORKSPACE_MEMBER1"})
    void AuthorisedEditNewCollectionAsMember_shouldFailWith403() throws Exception {
        mvc.perform(get("/edit/collection/?workspaceIds=1").contentType(MediaType.TEXT_HTML))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username="test-workspace-manager1", roles = {"WORKSPACE_MANAGER1"})
    void AuthorisedEditNewCollection_shouldSucceedWith200() throws Exception {
        mvc.perform(get("/edit/collection/?workspaceIds=1").contentType(MediaType.TEXT_HTML))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(username="test-workspace-manager1", roles = {"WORKSPACE_MANAGER1"})
    void AuthorisedEditNewCollectionManager_shouldFailWith403() throws Exception {
        mvc.perform(get("/edit/collection/?workspaceIds=2").contentType(MediaType.TEXT_HTML))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    void UnauthorisedEditCollection_shouldFailWith401() throws Exception {
        mvc.perform(get("/edit/collection/?collectionId=collections/test.collection.json").contentType(MediaType.TEXT_HTML))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser(username="test-deployment-manager", roles = {"DEPLOYMENT_MANAGER"})
    void AuthorisedEditCollection_shouldFailWith403() throws Exception {
        mvc.perform(get("/edit/collection/?collectionId=collections/test.collection.json").contentType(MediaType.TEXT_HTML))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username="test-workspace-member1", roles = {"WORKSPACE_MEMBER1"})
    void AuthorisedEditCollectionAsMember_shouldSucceedWith200() throws Exception {
        mvc.perform(get("/edit/collection/?collectionId=collections/test.collection.json").contentType(MediaType.TEXT_HTML))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(username="test-workspace-manager1", roles = {"WORKSPACE_MANAGER1"})
    void AuthorisedEditCollection_shouldSucceedWith200() throws Exception {
        mvc.perform(get("/edit/collection/?collectionId=collections/test.collection.json").contentType(MediaType.TEXT_HTML))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(username="test-workspace-manager1", roles = {"WORKSPACE_MANAGER1"})
    void AuthorisedEditCollectionWrongManager_shouldFailWith403() throws Exception {
        mvc.perform(get("/edit/collection/?collectionId=2").contentType(MediaType.TEXT_HTML))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    void UnauthorisedGetData_shouldFailWith401() throws Exception {
        mvc.perform(get(pathForDataDisplay+"/items/data/tei/MS-TEST-00001/MS-TEST-00001.xml").contentType(MediaType.TEXT_HTML))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser(username="test-workspace-member1", roles = {"WORKSPACE_MEMBER1"})
    void AuthorisedGetDataAsMember_shouldSucceedWith200() throws Exception {
        mvc.perform(get(pathForDataDisplay+"/items/data/tei/MS-TEST-00001/MS-TEST-00001.xml").contentType(MediaType.TEXT_HTML))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void UnauthorisedUpdateCollection_shouldFailWith401() throws Exception {
        mvc.perform(post("/edit/collection/update?workspaceIds=1").contentType(MediaType.TEXT_HTML)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("itemIds", "../items/data/tei/MS-TEST-00001/MS-TEST-00001.xml")
            .param("collectionId", "collections/test.collection.json")
            .param("urlSlugName", "test")
            .param("shortName", "Test Collection")
            .param("fullName", "This is a test collection")
            .param("sortName", "Sorting name")
            .param("thumbnailURL", "./pages/images/collectionsView/collection-blank.jpg")
            .param("shortDescription", "Test collection. Short Description.trtj Test")
            .param("mediumDescription", "Test collection.")
            .param("fullDescriptionHTML", "<h1>Test Collection</h1>")
            .param("proseCreditPath", "../pages/html/collections/test/sponsors.html"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser(username="test-workspace-member1", roles = {"WORKSPACE_MEMBER1"})
    void AuthorisedUpdateCollection_shouldSucceedWith302() throws Exception {
        mvc.perform(post("/edit/collection/update?workspaceIds=1").contentType(MediaType.TEXT_HTML)

            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("itemIds", "../items/data/tei/MS-TEST-00001/MS-TEST-00001.xml")
            .param("collectionId", "collections/test.collection.json")
            .param("urlSlugName", "test")
            .param("shortName", "Test Collection")
            .param("fullName", "This is a test collection")
            .param("sortName", "Sorting name")
            .param("thumbnailURL", "./pages/images/collectionsView/collection-blank.jpg")
            .param("shortDescription", "Test collection. Short Description Test")
            .param("mediumDescription", "Test collection.")
            .param("fullDescriptionHTML", "Test Collection")
            .param("fullDescriptionPath", "../pages/html/collections/test/summary.html")
            .param("proseCreditHTML", "<hr>")
            .param("proseCreditPath", "../pages/html/collections/test/sponsors.html"))
            .andDo(print())
            .andExpect(MockMvcResultMatchers.status().isFound());
    }

    @Test
    @WithMockUser(username="test-workspace-member2", roles = {"WORKSPACE_MEMBER2"})
    void AuthorisedUpdateCollection_shouldFailWith403() throws Exception {
        mvc.perform(post("/edit/collection/update?workspaceIds=1").contentType(MediaType.TEXT_HTML)

            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("itemIds", "../items/data/tei/MS-TEST-00001/MS-TEST-00001.xml")
            .param("collectionId", "collections/test.collection.json")
            .param("urlSlugName", "test")
            .param("shortName", "Test Collection")
            .param("fullName", "This is a test collection")
            .param("sortName", "Sorting name")
            .param("thumbnailURL", "./pages/images/collectionsView/collection-blank.jpg")
            .param("shortDescription", "Test collection. Short Description Test")
            .param("mediumDescription", "Test collection.")
            .param("fullDescriptionHTML", "Test Collection")
            .param("fullDescriptionPath", "../pages/html/collections/test/summary.html")
            .param("proseCreditHTML", "<hr>")
            .param("proseCreditPath", "../pages/html/collections/test/sponsors.html"))
            .andDo(print())
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    void UnauthorisedUpdateNewCollection_shouldFailWith401() throws Exception {
        mvc.perform(post("/edit/collection/update?workspaceIds=1").contentType(MediaType.TEXT_HTML)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("itemIds", "../items/data/tei/MS-TEST-00001/MS-TEST-00001.xml")
            .param("collectionId", "collections/new.collection.json")
            .param("urlSlugName", "newcollection")
            .param("shortName", "Test Collection")
            .param("fullName", "This is a test collection")
            .param("sortName", "Sorting name")
            .param("thumbnailURL", "./pages/images/collectionsView/collection-blank.jpg")
            .param("shortDescription", "Test collection. Short Description.trtj Test")
            .param("mediumDescription", "Test collection.")
            .param("fullDescriptionHTML", "<h1>Test Collection</h1>")
            .param("fullDescriptionPath", "../pages/html/collections/test/summary.html")
            .param("proseCreditHTML", "<hr>")
            .param("proseCreditPath", "../pages/html/collections/test/sponsors.html"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser(username="test-workspace-member1", roles = {"WORKSPACE_MEMBER1"})
    void AuthorisedUpdateNewCollectionMember_shouldFailWith403() throws Exception {
        mvc.perform(post("/edit/collection/update?workspaceIds=1").contentType(MediaType.TEXT_HTML)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("itemIds", "../items/data/tei/MS-TEST-00001/MS-TEST-00001.xml")
            .param("collectionId", "collections/new.collection.json")
            .param("urlSlugName", "newcollection")
            .param("shortName", "Test Collection")
            .param("fullName", "This is a test collection")
            .param("sortName", "Sorting name")
            .param("thumbnailURL", "./pages/images/collectionsView/collection-blank.jpg")
            .param("shortDescription", "Test collection. Short Description.trtj Test")
            .param("mediumDescription", "Test collection.")
            .param("fullDescriptionHTML", "<h1>Test Collection</h1>")
            .param("fullDescriptionPath", "../pages/html/collections/test/summary.html")
            .param("proseCreditHTML", "<hr>")
            .param("proseCreditPath", "../pages/html/collections/test/sponsors.html"))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username="test-workspace-manager2", roles = {"WORKSPACE_MANAGER2"})
    void AuthorisedUpdateNewCollection_shouldFailWith403() throws Exception {
        mvc.perform(post("/edit/collection/update?workspaceIds=1").contentType(MediaType.TEXT_HTML)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("itemIds", "../items/data/tei/MS-TEST-00001/MS-TEST-00001.xml")
            .param("collectionId", "collections/new.collection.json")
            .param("urlSlugName", "newcollection")
            .param("shortName", "Test Collection")
            .param("fullName", "This is a test collection")
            .param("sortName", "Sorting name")
            .param("thumbnailURL", "./pages/images/collectionsView/collection-blank.jpg")
            .param("shortDescription", "Test collection. Short Description.trtj Test")
            .param("mediumDescription", "Test collection.")
            .param("fullDescriptionHTML", "<h1>Test Collection</h1>")
            .param("fullDescriptionPath", "../pages/html/collections/test/summary.html")
            .param("proseCreditHTML", "<hr>")
            .param("proseCreditPath", "../pages/html/collections/test/sponsors.html"))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username="test-workspace-manager1", roles = {"WORKSPACE_MANAGER1"})
    void AuthorisedUpdateNewCollection_shouldSucceedWith302() throws Exception {
        mvc.perform(post("/edit/collection/update?workspaceIds=1")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("itemIds", "../items/data/tei/MS-TEST-00001/MS-TEST-00001.xml")
            .param("collectionId", "collections/new.collection.json")
            .param("urlSlugName", "newcollection")
            .param("shortName", "Test Collection")
            .param("fullName", "This is a test collection")
            .param("sortName", "Sorting name")
            .param("thumbnailURL", "./pages/images/collectionsView/collection-blank.jpg")
            .param("shortDescription", "Test collection. Short Description.trtj Test")
            .param("mediumDescription", "Test collection.")
            .param("fullDescriptionHTML", "<h1>Test Collection</h1>")
            .param("fullDescriptionPath", "../pages/html/collections/test/summary.html")
            .param("proseCreditHTML", "<hr>")
            .param("proseCreditPath", "../pages/html/collections/test/sponsors.html"))
            .andExpect(MockMvcResultMatchers.status().isFound());
    }

    ///edit/collection/addItem
    @Test
    void UnauthorisedAddItem_shouldFailWith401() throws Exception {

        MockMultipartFile xmlFile = new MockMultipartFile("file", "MS-TEST-00002.xml", "text/plain", "some xml".getBytes());
        mvc.perform(multipart("/edit/collection/addItem?collectionId=collections/test.collection.json")
            .file(xmlFile))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser(username="test-workspace-member2", roles = {"WORKSPACE_MEMBER2"})
    void AuthorisedAddItem_shouldFailWith403() throws Exception {

        MockMultipartFile xmlFile = new MockMultipartFile("file", "MS-TEST-00002.xml", "text/plain", "some xml".getBytes());
        mvc.perform(multipart("/edit/collection/addItem?collectionId=collections/test.collection.json")
            .file(xmlFile))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username="test-workspace-member1", roles = {"WORKSPACE_MEMBER1"})
    void AuthorisedAddItem_shouldSucceedWith302() throws Exception {

        MockMultipartFile xmlFile = new MockMultipartFile("file", "MS-TEST-00002.xml", "text/plain", "some xml".getBytes());
        mvc.perform(multipart("/edit/collection/addItem?collectionId=collections/test.collection.json")
            .file(xmlFile))
            .andExpect(MockMvcResultMatchers.status().isFound());
    }

    @Test
    void UnauthorisedDeleteItem_shouldFailWith401() throws Exception {

        mvc.perform(post("/edit/collection/deleteItem?itemId=collectionId=collections/test.collection.json")
            .queryParam("collectionId","collections/test.collection.json")
            .queryParam("itemName","MS-TEST-00001"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser(username="test-workspace-member1", roles = {"WORKSPACE_MEMBER1"})
    void AuthorisedDeleteItem_shouldSucceed302() throws Exception {

        mvc.perform(post("/edit/collection/deleteItem?itemId=collectionId=collections/test.collection.json")
            .queryParam("collectionId","collections/test.collection.json")
            .queryParam("itemName","MS-TEST-00001"))
            .andExpect(MockMvcResultMatchers.status().isFound());
    }

    @Test
    @WithMockUser(username="test-workspace-member2", roles = {"WORKSPACE_MEMBER2"})
    void AuthorisedDeleteItem_shouldFailWith403() throws Exception {

        mvc.perform(post("/edit/collection/deleteItem?itemId=collectionId=collections/test.collection.json")
            .queryParam("collectionId","collections/test.collection.json")
            .queryParam("itemName","MS-TEST-00001"))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
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