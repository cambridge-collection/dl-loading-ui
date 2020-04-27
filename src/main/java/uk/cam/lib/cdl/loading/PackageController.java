package uk.cam.lib.cdl.loading;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriUtils;
import uk.cam.lib.cdl.loading.apis.PackagingAPI;
import uk.cam.lib.cdl.loading.model.Tag;
import uk.cam.lib.cdl.loading.model.packaging.PackagingStatus;
import uk.cam.lib.cdl.loading.model.packaging.Pipeline;
import uk.cam.lib.cdl.loading.model.packaging.Update;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/package")
public class PackageController {

    private final PackagingAPI packagingAPI;

    @Autowired
    public PackageController(PackagingAPI packagingAPI) {
        this.packagingAPI = packagingAPI;
    }

    @GetMapping ("/package.html")
    @PreAuthorize("@roleService.canBuildPackages(authentication)")
    public String pack(Model model) {

        List<Update> updates = packagingAPI.updatesSinceLastPackage();
        Collections.sort(updates);
        model.addAttribute("updates", updates);

        List<Pipeline> pipelines = packagingAPI.getHistory();
        model.addAttribute("pipelines", pipelines);

        boolean showChanges = true;
        HashMap<String, Tag> tagLookup = new HashMap<>();
        for (Pipeline pipeline : pipelines) {

            // Hide the edits / start new pipeline button if there is a pipeline already running.
            String type = pipeline.getStatus().getType();
            if ("pipeline_state_pending".equals(type) || "pipeline_state_queued".equals(type) ||
                "pipeline_state_in_progress".equals(type)) {
                showChanges = false;
            }

            // Match tags to builds by build number.
            // THIS ASSUMES BUILD NUMBER is at end of tag e.g. ...v123
            // YOU WILL NEED TO CHANGE THIS IF TAG FORMAT IS CHANGED.
            // TODO update build status from pipeline with result info
            // https://developer.atlassian.com/server/bitbucket/how-tos/updating-build-status-for-commits/
            List<Tag> tags = packagingAPI.getTags();
            for (Tag tag : tags) {
                if (tag.getName().endsWith("v" + pipeline.getBuildNumber())) {
                    tagLookup.put(pipeline.getId(), tag);
                    break;
                }
            }
        }
        model.addAttribute("showChanges", showChanges);
        model.addAttribute("tagLookup", tagLookup);

        return "package";
    }

    @GetMapping ("/startProcess")
    @PreAuthorize("@roleService.canBuildPackages(authentication)")
    public RedirectView startProcess(Model model) {
        String processId = packagingAPI.startProcess();

        return new RedirectView("/package/" + UriUtils.encodePathSegment(processId, StandardCharsets.UTF_8) + "/status");
    }

    @GetMapping("/{id}/status")
    @PreAuthorize("@roleService.canBuildPackages(authentication)")
    public String status(Model model, @PathVariable("id") String id) {
        PackagingStatus status = packagingAPI.getStatus(id);
        model.addAttribute("status", status);

        return "package-status";
    }
}
