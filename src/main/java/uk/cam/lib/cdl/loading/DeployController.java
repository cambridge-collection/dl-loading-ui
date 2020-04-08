package uk.cam.lib.cdl.loading;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import uk.cam.lib.cdl.loading.apis.DeploymentAPI;
import uk.cam.lib.cdl.loading.apis.PackagingAPI;
import uk.cam.lib.cdl.loading.model.Tag;
import uk.cam.lib.cdl.loading.model.deployment.Deployment;
import uk.cam.lib.cdl.loading.model.deployment.Instance;
import uk.cam.lib.cdl.loading.model.deployment.Status;

import java.util.Collections;
import java.util.List;

@Controller
public class DeployController {

    private final DeploymentAPI deploymentAPI;
    private final PackagingAPI packagingAPI;

    @Autowired
    public DeployController(DeploymentAPI deploymentAPI, PackagingAPI packagingAPI) {
        this.deploymentAPI = deploymentAPI;
        this.packagingAPI = packagingAPI;
    }

    /**
     * Displays the deployment page with the table.
     *
     * @param model
     * @return
     */
    @PreAuthorize("@roleService.canDeploySites(authentication)")
    @GetMapping ("/deploy/deploy.html")
    public String deploy(Model model, @ModelAttribute("message") String message,
                         @ModelAttribute("error") String error) {

        List<Instance> instances = deploymentAPI.getInstances();
        // NOTE this gets the tags from the source repo instead of from the release repo, but they should be
        // the same. Could checkout the release repo, or use BitBucket API for access to release repo directly.
        List<Tag> tags = packagingAPI.getTags();
        Collections.sort(tags);
        Collections.sort(instances);

        model.addAttribute("instances", instances);
        model.addAttribute("tags", tags);
        if (message != null && !message.isEmpty()) {
            model.addAttribute("message", message);
        }
        if (error != null && !error.isEmpty()) {
            model.addAttribute("error", error);
        }
        return "deploy";
    }

    @PreAuthorize("@roleService.canDeploySites(authentication)")
    @GetMapping("/deploy/cache/refresh")
    public String deployRefreshCache(Model model, @ModelAttribute("message") String message,
                                     @ModelAttribute("error") String error) {

        deploymentAPI.cacheEvict();
        return deploy(model, message, error);
    }

    /**
     * Updates the table to trigger a deployment next puppet run.
     *
     * @param attributes
     * @param instanceId
     * @param version
     * @return
     * @throws JSONException
     */
    @PreAuthorize("@roleService.canDeploySites(authentication)")
    @PostMapping("/deploy/{instanceId}")
    public RedirectView deployVersion(RedirectAttributes attributes, @PathVariable("instanceId") String instanceId,
                                      @RequestParam String version) throws JSONException {
        /** TODO validate input **/
        Instance instance = deploymentAPI.getInstance(instanceId);
        instance.setVersion(version);
        boolean returnOK = deploymentAPI.setInstance(instance);
        if (returnOK) {
            String message = "Deployment process has started for " + version + " to instance " + instanceId + ".  "
                + " This may take a few minutes to complete.";

            attributes.addFlashAttribute("message", message);
            deploymentAPI.cacheEvict();

        } else {

            attributes.addFlashAttribute("error", "There was an error deploying your version.");

        }
        return new RedirectView("/deploy/status/" + instanceId + "/");
    }

    @PreAuthorize("@roleService.canDeploySites(authentication)")
    @GetMapping("/deploy/status/{instanceId}")
    public String deployStatus(Model model, @PathVariable("instanceId") String instanceId) {

        Deployment deployment = new Deployment();
        Instance instance = deploymentAPI.getInstance(instanceId);
        Status status = deploymentAPI.getStatus(instanceId);
        deployment.setInstanceRequest(instance);
        deployment.setInstanceStatus(status);

        if (status == null | status.getCurrentCollectionsVersion() == null || status.getCurrentItemsVersion() == null) {
            model.addAttribute("error", "There was an error getting the status information for this instance.");
        }
        if (instance == null) {
            model.addAttribute("error", "There was an error getting details for this instance.");
        }
        if (instance.getVersion().equals(status.getCurrentCollectionsVersion()) &&
            instance.getVersion().equals(status.getCurrentItemsVersion())) {
            deployment.setDeploymentComplete(true);
        } else {
            deployment.setDeploymentComplete(false);
        }

        model.addAttribute("deployment", deployment);
        return "deploy-status";
    }

}
