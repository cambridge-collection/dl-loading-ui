package uk.cam.lib.cdl.loading.security.saml;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import uk.cam.lib.cdl.loading.security.UsersConfig;

import static com.google.common.base.Preconditions.checkNotNull;

public class SAMLUserDetailsServiceImpl implements SAMLUserDetailsService {

    public static final String DEFAULT_FIRST_NAME_ATTRIBUTE = "urn:oid:2.5.4.42";
    public static final String DEFAULT_LAST_NAME_ATTRIBUTE = "urn:oid:2.5.4.4";
    public static final String DEFAULT_EMAIL_ATTRIBUTE = "urn:oid:1.2.840.113549.1.9.1";

    private final UserDetailsService userDetailsService;
    private final String attrFirstName;
    private final String attrEmail;
    private final String attrLastName;

    public SAMLUserDetailsServiceImpl(UserDetailsService userDetailsService) {
        this(userDetailsService, DEFAULT_FIRST_NAME_ATTRIBUTE, DEFAULT_LAST_NAME_ATTRIBUTE, DEFAULT_EMAIL_ATTRIBUTE);
    }

    public SAMLUserDetailsServiceImpl(
            @Qualifier(UsersConfig.USER_DETAILS_SERVICE)
            UserDetailsService userDetailsService,
            String firstNameAttribute,
            String lastNameAttribute,
            String emailAttribute
    ) {
        this.userDetailsService = checkNotNull(userDetailsService, "userDetailsService was null");
        this.attrFirstName = checkNotNull(firstNameAttribute, "firstNameAttribute was null");
        this.attrLastName = checkNotNull(lastNameAttribute, "lastNameAttribute was null");
        this.attrEmail = checkNotNull(emailAttribute, "emailAttribute was null");
    }

    public UserDetails loadUserBySAML(SAMLCredential credential)
        throws UsernameNotFoundException {

        String userID = credential.getNameID().getValue();

        /*
        // Not currently using other attributes values.
        // Could use these to populate user info when auto-creating users on first login.
        String givenName = credential.getAttributeAsString(attrFirstName);
        String surname = credential.getAttributeAsString(attrLastName);
        String email = credential.getAttributeAsString(attrEmail);
        */

        return checkNotNull(this.userDetailsService.loadUserByUsername(userID));
    }
}


