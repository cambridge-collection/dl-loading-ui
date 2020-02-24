package uk.cam.lib.cdl.loading.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml.metadata.MetadataManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Set;

@Controller
@RequestMapping("/saml")
public class SSOController {

    private static final Logger LOG = LoggerFactory.getLogger(SSOController.class);

    private final MetadataManager metadata;

    public SSOController(MetadataManager metadata) {
        this.metadata = metadata;
    }

    @RequestMapping(value = "/discovery", method = RequestMethod.GET)
    public String idpSelection(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || (auth instanceof AnonymousAuthenticationToken)) {
            Set<String> idps = metadata.getIDPEntityNames();
            for (String idp : idps) {
                LOG.info("Configured IdP: " + idp);
            }
            model.addAttribute("idps", idps);
            return "login";
        } else {
            // User already logged in
            return "redirect:/";
        }
    }

}

