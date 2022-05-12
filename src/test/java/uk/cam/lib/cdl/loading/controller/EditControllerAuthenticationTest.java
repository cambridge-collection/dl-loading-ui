package uk.cam.lib.cdl.loading.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.cam.lib.cdl.loading.dao.UserRepository;
import uk.cam.lib.cdl.loading.dao.WorkspaceRepository;
import uk.cam.lib.cdl.loading.model.editor.Workspace;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
class EditControllerAuthenticationTest {

    @TestConfiguration
 //   @Import(GitRepoWithTestDataConfig.class)
    public static class Config { }

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private WebApplicationContext context;

/*    @Autowired
    private EditAPI editAPI;*/

    private MockMvc mvc;

    @Value("/edit"+"${data.url.display}")
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

/*        File testSourceDirOriginals = new File("./src/test/resources/source-data/data/");
        when(editAPI.getDataLocalPath()).thenReturn(Path.of(testSourceDirOriginals.getAbsolutePath()));

        File mockFile = new File (testSourceDirOriginals.getAbsolutePath()+"/collections/test.collection.json");
        when(editAPI.getCollectionPath("collections/test.collection.json")).thenReturn(mockFile.toPath());

        Collection mockCollection = new Collection(name, description, credit, itemIds, ImmutableList.of());
        when(editAPI.getCollection("collections/test.collection.json")).thenReturn(mockCollection);*/
    }

    @Test
    void UnauthorisedEditScreen_shouldFailWith401() throws Exception {
        mvc.perform(get("/edit/edit.html"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @WithMockUser(username="test-deployment-manager", roles = {"DEPLOYMENT_MANAGER"})
    @Test
    void givenAuthRequestOnEditScreenDeployManager_shouldFailWIth403() throws Exception {
        mvc.perform(get("/edit/edit.html"))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @WithMockUser(username="test-workspace-manager1", roles = {"WORKSPACE_MANAGER1"})
    @Test
    void givenAuthRequestOnEditScreenWorkspaceManager_shouldSucceedWith200() throws Exception {
        mvc.perform(get("/edit/edit.html"))
            .andExpect(MockMvcResultMatchers.status().isOk());

    }

    @WithMockUser(username="test-workspace-member1", roles = {"WORKSPACE_MEMBER1"})
    @Test
    void givenAuthRequestOnEditScreenWorkspaceMember_shouldSucceedWith200() throws Exception {
        mvc.perform(get("/edit/edit.html"))
            .andExpect(MockMvcResultMatchers.status().isOk());

    }

    @Test
    void UnauthorisedEditNewCollection_shouldFailWith401() throws Exception {
        mvc.perform(get("/edit/collection/?workspaceIds=1"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser(username="test-deployment-manager", roles = {"DEPLOYMENT_MANAGER"})
    void AuthorisedEditNewCollection_shouldFailWith403() throws Exception {
        mvc.perform(get("/edit/collection/?workspaceIds=1"))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username="test-workspace-member1", roles = {"WORKSPACE_MEMBER1"})
    void AuthorisedEditNewCollectionAsMember_shouldFailWith403() throws Exception {
        mvc.perform(get("/edit/collection/?workspaceIds=1"))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username="test-workspace-manager1", roles = {"WORKSPACE_MANAGER1"})
    void AuthorisedEditNewCollection_shouldSucceedWith200() throws Exception {
        mvc.perform(get("/edit/collection/?workspaceIds=1"))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(username="test-workspace-manager1", roles = {"WORKSPACE_MANAGER1"})
    void AuthorisedEditNewCollectionManager_shouldFailWith403() throws Exception {
        mvc.perform(get("/edit/collection/?workspaceIds=2"))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    void UnauthorisedEditCollection_shouldFailWith401() throws Exception {
        mvc.perform(get("/edit/collection/?collectionId=collections/test.collection.json"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser(username="test-deployment-manager", roles = {"DEPLOYMENT_MANAGER"})
    void AuthorisedEditCollection_shouldFailWith403() throws Exception {
        mvc.perform(get("/edit/collection/?collectionId=collections/test.collection.json"))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    // FIXME
/*    @Test
    @WithMockUser(username="test-workspace-member1", roles = {"WORKSPACE_MEMBER1"})
    void AuthorisedEditCollectionAsMember_shouldSucceedWith200() throws Exception {
        mvc.perform(get("/edit/collection/?collectionId=collections/test.collection.json"))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }*/

    // FIXME
/*    @Test
    @WithMockUser(username="test-workspace-manager1", roles = {"WORKSPACE_MANAGER1"})
    void AuthorisedEditCollection_shouldSucceedWith200() throws Exception {
        mvc.perform(get("/edit/collection/?collectionId=collections/test.collection.json"))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }*/

    @Test
    @WithMockUser(username="test-workspace-manager1", roles = {"WORKSPACE_MANAGER1"})
    void AuthorisedEditCollectionWrongManager_shouldFailWith403() throws Exception {
        mvc.perform(get("/edit/collection/?collectionId=2"))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    void UnauthorisedGetData_shouldFailWith401() throws Exception {
        mvc.perform(get(pathForDataDisplay+"/items/data/tei/MS-TEST-00001/MS-TEST-00001.xml"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    // FIXME
/*    @Test
    @WithMockUser(username="test-workspace-member1", roles = {"WORKSPACE_MEMBER1"})
    void AuthorisedGetDataAsMember_shouldSucceedWith200() throws Exception {
        mvc.perform(get(pathForDataDisplay+"items/data/tei/MS-TEST-00001/MS-TEST-00001.xml"))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }*/

    @Test
    void UnauthorisedUpdateCollection_shouldFailWith401() throws Exception {
        mvc.perform(post("/edit/collection/update?workspaceIds=1")
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
        mvc.perform(post("/edit/collection/update?workspaceIds=1")

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
        mvc.perform(post("/edit/collection/update?workspaceIds=1")

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
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser(username="test-workspace-member1", roles = {"WORKSPACE_MEMBER1"})
    void AuthorisedUpdateNewCollectionMember_shouldFailWith403() throws Exception {
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
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username="test-workspace-manager2", roles = {"WORKSPACE_MANAGER2"})
    void AuthorisedUpdateNewCollection_shouldFailWith403() throws Exception {
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

    @Test
    void UnauthorisedDeleteItem_shouldFailWith401() throws Exception {

        mvc.perform(post("/edit/collection/deleteItem")
            .queryParam("collectionId","collections/test.collection.json")
            .queryParam("itemId", "items/data/tei/MS-TEST-00001/MS-TEST-00001.xml"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser(username="test-workspace-member1", roles = {"WORKSPACE_MEMBER1"})
    void AuthorisedDeleteItem_shouldSucceed302() throws Exception {

        mvc.perform(post("/edit/collection/deleteItem")
            .queryParam("collectionId","collections/test.collection.json")
            .queryParam("itemId","items/data/tei/MS-TEST-00001/MS-TEST-00001.xml"))
            .andExpect(MockMvcResultMatchers.status().isFound());
    }

    @Test
    @WithMockUser(username="test-workspace-member2", roles = {"WORKSPACE_MEMBER2"})
    void AuthorisedDeleteItem_shouldFailWith403() throws Exception {

        mvc.perform(post("/edit/collection/deleteItem")
            .queryParam("collectionId","collections/test.collection.json")
            .queryParam("itemId", "items/data/tei/MS-TEST-00001/MS-TEST-00001.xml"))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }
}
