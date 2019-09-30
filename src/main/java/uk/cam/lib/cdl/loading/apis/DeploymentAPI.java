package uk.cam.lib.cdl.loading.apis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import uk.cam.lib.cdl.loading.model.Instance;

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

    @CacheEvict(allEntries = true, value = {cacheName})
    @Scheduled(fixedDelay = 5 * 60 * 1000, initialDelay = 500) // Every 5 mins
    public void reportCacheEvict() {
        System.out.println("Flush Cache " + DateFormat.getInstance().format(new Date()));
    }

}
