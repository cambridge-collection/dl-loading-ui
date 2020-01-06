package uk.cam.lib.cdl.loading.apis;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.cam.lib.cdl.loading.config.EditConfig;
import uk.cam.lib.cdl.loading.config.GitConfig;
import uk.cam.lib.cdl.loading.model.deployment.Instance;
import uk.cam.lib.cdl.loading.model.deployment.Status;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {EditConfig.class, GitConfig.class})
@AutoConfigureWireMock(port = 8089)
class DeploymentAPITest {

    private DeploymentAPI deploymentAPI;

    public DeploymentAPITest() throws MalformedURLException {

        String url = "http://localhost:8089/api/deploy/v0.1/";
        deploymentAPI = new DeploymentAPI(new URL(url));

    }

    @Test
    void getInstances() {
        final List<Instance> instances = deploymentAPI.getInstances();
        assert (instances.size() == 1);
        Instance instance = instances.get(0);
        assert (instance.toString().equals("class Instance {\n" +
            "    displayOrder: 0\n" +
            "    instanceId: string\n" +
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
