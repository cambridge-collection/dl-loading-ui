package uk.cam.lib.cdl.loading;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.cam.lib.cdl.loading.apis.BitBucketAPI;
import uk.cam.lib.cdl.loading.apis.PackagingAPI;
import uk.cam.lib.cdl.loading.apis.PackagingStatus;

@Controller
public class PackageController {

    private final PackagingAPI packagingAPI;
    private final BitBucketAPI bitbucketAPI;

    @Autowired
    public PackageController(PackagingAPI packagingAPI, @Qualifier("sourceRepo") BitBucketAPI bitbucketAPI) {
        this.packagingAPI = packagingAPI;
        this.bitbucketAPI = bitbucketAPI;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/package/package.html")
    public String pack(Model model) {

        int count = packagingAPI.commitsSinceLastPackage();
        model.addAttribute("commitsSinceLastPackage", count);

        return "package";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/package/startProcess")
    public String startProcess(Model model) {
        String processId = packagingAPI.startProcess();
        PackagingStatus status = packagingAPI.getStatus(processId);

        model.addAttribute("status", status);

        return "package-status";
    }
}
