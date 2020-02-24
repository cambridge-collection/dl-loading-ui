package uk.cam.lib.cdl.loading;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import uk.cam.lib.cdl.loading.security.MyUserDetails;

@Controller
public class HomeController {

    @GetMapping({"/", "/index.html"})
    public String index(Model model) {

        Object authentication =  SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = "Unknown";
        String firstName = "";
        String lastName = "";
        if (authentication instanceof MyUserDetails) {
            username = ((MyUserDetails) authentication).getUsername();
            firstName = ((MyUserDetails) authentication).getFirstName();
            lastName = ((MyUserDetails) authentication).getLastName();
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
