package uk.cam.lib.cdl.loading;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import uk.cam.lib.cdl.loading.utils.DeploymentHelper;

import java.io.IOException;

@Controller
@RequestMapping ("/deploy")
public class DeployController {

    private final DeploymentHelper deploymentHelper;

    public DeployController(@Value("${data.aws.region}") String region, @Value("${deploy.releases.staging.bucketname}") String sourceBucket,
                            @Value("${deploy.releases.production.bucketname}") String destBucket) {

        this.deploymentHelper = new DeploymentHelper(region,sourceBucket,destBucket);
    }
    /**
     * Displays the deployment page with the table.
     *
     * @param model
     * @return
     */
    @PreAuthorize("@roleService.canDeploySites(authentication)")
    @GetMapping ("/deploy.html")
    public String deploy(Model model, @ModelAttribute("message") String message,
                         @ModelAttribute("error") String error) {

        // For the moment we will take details for the "staging" and "production" s3 buckets
        // from properties file.

        if (message != null && !message.isEmpty()) {
            model.addAttribute("message", message);
        }
        if (error != null && !error.isEmpty()) {
            model.addAttribute("error", error);
        }
        return "deploy";
    }


    /**
     * Deploy data to production.  This involves copying data from the staging bucket to the production bucket.
     *
     * @param attributes
     * @return
     * @throws JSONException
     */
    @PreAuthorize("@roleService.canDeploySites(authentication)")
    @PostMapping("/production")
    public RedirectView deployToProduction(RedirectAttributes attributes) throws IOException, InterruptedException {

        boolean returnOK = deploymentHelper.deploy();

        if (returnOK) {
            String message = "Deployment process has started.  "
                + " This may take a few minutes to complete.";

            attributes.addFlashAttribute("message", message);

        } else {

            attributes.addFlashAttribute("error", "There was an error deploying your version.");

        }
        return new RedirectView("/deploy/deploy.html");
    }


}
