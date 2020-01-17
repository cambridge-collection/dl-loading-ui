package uk.cam.lib.cdl.loading;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping({"/", "/index.html"})
    public String index(Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = "Unknown";
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            username = authentication.getName();
        }
        model.addAttribute("username", username);

        return "home";
    }

    @GetMapping("/blank.html")
    public String blank(Model model) {

        return "blank";
    }

    @GetMapping("/login/login.html")
    public String login(Model model) {

        return "login";
    }

    @GetMapping("/login/forgot-password.html")
    public String forgotPassword(Model model) {

        return "forgot-password";
    }

    @GetMapping("/login/register.html")
    public String register(Model model) {

        return "register";
    }

}
