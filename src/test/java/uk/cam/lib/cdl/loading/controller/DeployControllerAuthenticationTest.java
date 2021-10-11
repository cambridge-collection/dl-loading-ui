package uk.cam.lib.cdl.loading.controller;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.UriComponentsBuilder;
import uk.cam.lib.cdl.loading.apis.DeploymentAPI;
import uk.cam.lib.cdl.loading.dao.UserRepository;
import uk.cam.lib.cdl.loading.dao.WorkspaceRepository;

import java.io.IOException;
import java.net.URI;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
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
    void setup() throws IOException {

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

        mvc.perform(get("/deploy/deploy.html"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());

    }

    @WithMockUser(username="test-deployment-manager", roles = {"DEPLOYMENT_MANAGER"})
    @Test
    void givenAuthRequestOnDeployScreenDeployManager_shouldSucceedWith200() throws Exception {
        mvc.perform(get("/deploy/deploy.html"))
            .andExpect(MockMvcResultMatchers.status().isOk());

    }

    @Test
    void UnauthorisedRefreshCache_shouldFailWith401() throws Exception {
        mvc.perform(get("/deploy/cache/refresh"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser(username="test-workspace-member1", roles = {"WORKSPACE_MEMBER1"})
    void AuthorisedRefreshCache_shouldFailWith403() throws Exception {
        mvc.perform(get("/deploy/cache/refresh"))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username="test-deployment-manager", roles = {"DEPLOYMENT_MANAGER"})
    void AuthorisedRefreshCache_shouldSucceedWith200() throws Exception {
        mvc.perform(get("/deploy/cache/refresh"))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void UnauthorisedDeployInstance_shouldFailWith401() throws Exception {
        mvc.perform(post("/deploy/testid"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser(username="test-workspace-manager1", roles = {"WORKSPACE_MANAGER1"})
    void AuthorisedDeployInstance_shouldFailWith403() throws Exception {
        // TODO
        //mvc.perform(post("/deploy/testid"))
        //    .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username="test-deployment-manager", roles = {"DEPLOYMENT_MANAGER"})
    void AuthorisedDeployInstance_shouldSucceedWith200() throws Exception {
        // TODO
        //mvc.perform(post("/deploy/testid"))
        //    .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void UnauthorisedDeployStatus_shouldFailWith401() throws Exception {
        mvc.perform(get("/deploy/status/testid"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser(username="test-workspace-manager1", roles = {"WORKSPACE_MANAGER1"})
    void AuthorisedDeployStatus_shouldFailWith403() throws Exception {
        mvc.perform(get("/deploy/status/testid"))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username="test-deployment-manager", roles = {"DEPLOYMENT_MANAGER"})
    void AuthorisedDeployStatus_shouldSucceedWith200() throws Exception {
        // TODO
        //mvc.perform(get("/deploy/status/testid"))
        //    .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
