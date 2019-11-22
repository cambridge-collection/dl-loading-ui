package uk.cam.lib.cdl.loading;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class HomeController {

    @RequestMapping(method = RequestMethod.GET, value = {"/", "/index.html"})
    public String index(Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = "Unknown";
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            username = authentication.getName();
        }
        model.addAttribute("username", username);

        return "home";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/blank.html")
    public String blank(Model model) {

        return "blank";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/login/login.html")
    public String login(Model model) {

        return "login";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/login/forgot-password.html")
    public String forgotPassword(Model model) {

        return "forgot-password";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/login/register.html")
    public String register(Model model) {

        return "register";
    }

}
