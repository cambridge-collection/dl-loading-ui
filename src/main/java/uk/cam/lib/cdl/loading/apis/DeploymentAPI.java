package uk.cam.lib.cdl.loading.apis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import uk.cam.lib.cdl.loading.model.deployment.Instance;
import uk.cam.lib.cdl.loading.model.deployment.Status;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class DeploymentAPI extends WebAPI {

    private URL deploymentURL;
    private final String cacheName = "deploymentInstances";

    public DeploymentAPI(URL deploymentURL) {
        this.deploymentURL = deploymentURL;
    }

    @Cacheable(cacheName)
    public List<Instance> getInstances() {

        try {

            URL url = new URL(deploymentURL + "instances");
            String json = this.requestGET(url, "application/json");
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

    @Cacheable(cacheName)
    /** TODO validate input **/
    public Instance getInstance(String instanceId) {

        try {

            URL url = new URL(deploymentURL + "instances/" + instanceId);
            String json = this.requestGET(url, "application/json");
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, new TypeReference<Instance>() {
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


    /**
     * TODO validate input
     **/
    public boolean setInstance(Instance instance) {

        try {

            URL url = new URL(deploymentURL + "instances/" + instance.getInstanceId());

            // Build the POST request
            JSONObject postJSON = new JSONObject();
            postJSON.put("displayOrder", instance.getDisplayOrder());
            postJSON.put("instanceId", instance.getInstanceId());
            postJSON.put("url", instance.getUrl());
            postJSON.put("version", instance.getVersion());

            return this.requestPOSTJSON(url, postJSON);

        } catch (MalformedURLException e) {
            System.err.println("Invalid URL for DeploymentAPI.  Look at your application.properties.");
            e.printStackTrace();
        }

        return false;
    }

    @CacheEvict(allEntries = true, value = {cacheName})
    @Scheduled(fixedDelay = 1 * 60 * 1000, initialDelay = 500) // Every 1 min
    public void cacheEvict() {
        System.out.println("Flush Cache " + DateFormat.getInstance().format(new Date()));
    }


    public Status getStatus(String instanceId) {

        try {

            URL url = new URL(deploymentURL + "instances/" + instanceId + "/status");
            String json = this.requestGET(url, "application/json");
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, new TypeReference<Status>() {
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
