package uk.cam.lib.cdl.loading;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.cam.lib.cdl.loading.apis.BitbucketAPI;
import uk.cam.lib.cdl.loading.apis.DeploymentAPI;
import uk.cam.lib.cdl.loading.model.Instance;
import uk.cam.lib.cdl.loading.model.Tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Controller
public class HomeController {

    @Value("${spring.application.name}")
    String appName;

    @Autowired
    private DeploymentAPI deploymentAPI;

    @Autowired
    private BitbucketAPI bitbucketAPI;

    @RequestMapping(method = RequestMethod.GET, value = { "/", "/index.html"} )
    public String index(Model model) {

        model.addAttribute("appName", appName);
        return "home";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/blank.html")
    public String blank(Model model) {

        return "blank";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/login.html")
    public String login(Model model) {

        return "login";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/charts.html")
    public String charts(Model model) {

        return "charts";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/deploy.html")
    public String deploy(Model model) {

        List<Instance> instances = deploymentAPI.getInstances();
        List<Tag> tags = bitbucketAPI.getTags();
        Collections.sort(tags);
        Collections.sort(instances);

        model.addAttribute("instances", instances);
        model.addAttribute("tags", tags);
        return "deploy";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/forgot-password.html")
    public String forgotPassword(Model model) {

        return "forgot-password";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/register.html")
    public String register(Model model) {

        return "register";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/tables.html")
    public String tables(Model model) {

        return "tables";
    }


}
