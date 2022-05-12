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
import uk.cam.lib.cdl.loading.dao.UserRepository;
import uk.cam.lib.cdl.loading.dao.WorkspaceRepository;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureWireMock(port = 0)
class DeployControllerAuthenticationTest {

    @Autowired
    private WireMockServer wireMockServer;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void setup()  {

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
    void UnauthorisedDeployInstance_shouldFailWith401() throws Exception {
        mvc.perform(post("/deploy/production"))
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

}
