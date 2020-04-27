package uk.cam.lib.cdl.loading.apis;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.web.util.UriComponentsBuilder;
import uk.cam.lib.cdl.loading.model.deployment.Instance;
import uk.cam.lib.cdl.loading.model.deployment.Status;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;


@SpringBootTest
@AutoConfigureWireMock(port = 0)
class DeploymentAPITest {

    @Autowired
    private WireMockServer wireMockServer;

    private DeploymentAPI deploymentAPI;

    @BeforeEach
    public void setup() throws MalformedURLException {
        URI apiURL = UriComponentsBuilder.fromHttpUrl(wireMockServer.baseUrl())
            .path("/api/deploy/v0.1/").build().toUri();
        this.deploymentAPI = new DeploymentAPI(apiURL.toURL());
    }

    @Test
    void getInstances() {
        final List<Instance> instances = deploymentAPI.getInstances();
        assert (instances.size() == 1);
        Instance instance = instances.get(0);
        assert (instance.toString().equals("class Instance {\n" +
            "    displayOrder: 0\n" +
            "    instanceId: testid\n" +
            "    version: string\n" +
            "    url: string\n" +
            "}"));

    }

    @Test
    void getInstance() {

        Instance instance =  deploymentAPI.getInstance("testid");

        assert (instance.toString().equals("class Instance {\n" +
            "    displayOrder: 0\n" +
            "    instanceId: testid\n" +
            "    version: string\n" +
            "    url: string\n" +
            "}"));
    }

    @Test
    void setInstance() {
        //TODO
    }

    @Test
    void cacheEvict() {
        //TODO
    }

    @Test
    void getStatus() {

        Status status =  deploymentAPI.getStatus("testid");

        assert (status.toString().equals("class Status {\n" +
            "    instanceId: testid\n" +
            "    currentItemsVersion: dl-version-123\n" +
            "    currentCollectionsVersion: dl-version-123\n" +
            "}"));
    }
}
