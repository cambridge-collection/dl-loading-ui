package uk.cam.lib.cdl.loading.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SAMLUserDetailsServiceImpl implements SAMLUserDetailsService {

    @Value("${auth.saml.attr.firstName}")
    private String attrFirstName;

    @Value("${auth.saml.attr.lastName}")
    private String attrLastName;

    @Value("${auth.saml.attr.email}")
    private String attrEmail;

    public MyUserDetails loadUserBySAML(SAMLCredential credential)
        throws UsernameNotFoundException {

        String userID = credential.getNameID().getValue();

        String givenName = credential.getAttributeAsString(attrFirstName);
        String surname = credential.getAttributeAsString(attrLastName);
        String email = credential.getAttributeAsString(attrEmail);

        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
        authorities.add(authority);

        // TODO persist in a datastore
        // TODO Allow the recording of roles for specific users
        // TODO ensure userID is persistent.
        // Note: We don't want to record any personal data we don't need, so we can use the first/last names
        // for display when logged in but perhaps not store in DB.  Could store email?  Do we need to be
        // able to id accounts from this?
        return new MyUserDetails(userID, "", givenName, surname, email, true, authorities);
    }

}


