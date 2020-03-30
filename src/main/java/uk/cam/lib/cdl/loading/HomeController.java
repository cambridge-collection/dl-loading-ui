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
        if (authentication.getPrincipal() instanceof MyUserDetails) {
            User user = ((MyUserDetails) authentication.getPrincipal()).getUser();
            username = user.getUsername();
            firstName = user.getFirstName();
            lastName = user.getLastName();
        }
        model.addAttribute("username", username);
        model.addAttribute("firstName", firstName);
        model.addAttribute("lastName", lastName);

        return "home";
    }

    @GetMapping("/login/login.html")
    public String login(Model model) {

        return "login";
    }

}
