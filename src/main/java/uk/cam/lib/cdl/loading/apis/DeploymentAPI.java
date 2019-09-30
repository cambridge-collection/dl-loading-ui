package uk.cam.lib.cdl.loading.apis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.cam.lib.cdl.loading.model.Instance;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class DeploymentAPI extends WebAPI {

    private URL deploymentURL;

    public DeploymentAPI(URL deploymentURL) {
        this.deploymentURL = deploymentURL;
    }

    public List<Instance> getInstances() {

        try {

            URL url = new URL(deploymentURL + "instances");
            String json = this.requestGETJSON(url);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, new TypeReference<List<Instance>>() {
            });


        } catch (MalformedURLException e) {
            System.err.println("Invalid URL for DeploymentAPI.  Look at your application.properties.");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Problem connecting to the DeploymentAPI");
            e.printStackTrace();
        }

        return null;
    }

}
