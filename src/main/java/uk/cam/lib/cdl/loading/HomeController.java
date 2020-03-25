package uk.cam.lib.cdl.loading;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import uk.cam.lib.cdl.loading.model.security.User;
import uk.cam.lib.cdl.loading.security.MyUserDetails;

@Controller
public class HomeController {

    @GetMapping({"/", "/index.html"})
    public String index(Model model, Authentication authentication) {

        String username = "Unknown";
        String firstName = "";
        String lastName = "";
        if (authentication.getDetails() instanceof MyUserDetails) {
            User user = ((MyUserDetails) authentication.getDetails()).getUser();
            username = user.getUsername();
            firstName = user.getFirstName();
            lastName = user.getLastName();
        }
        model.addAttribute("username", username);
        model.addAttribute("firstName", firstName);
        model.addAttribute("lastName", lastName);

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
