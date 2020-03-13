package uk.cam.lib.cdl.loading.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.stereotype.Service;
import uk.cam.lib.cdl.loading.dao.UserRepository;
import uk.cam.lib.cdl.loading.model.security.User;

import javax.transaction.Transactional;

@Service
public class SAMLUserDetailsServiceImpl implements SAMLUserDetailsService {

    @Value("${auth.saml.attr.firstName}")
    private String attrFirstName;

    @Value("${auth.saml.attr.lastName}")
    private String attrLastName;

    @Value("${auth.saml.attr.email}")
    private String attrEmail;

    @Autowired
    private UserRepository userRepository;

    public UserDetails loadUserBySAML(SAMLCredential credential)  throws UsernameNotFoundException {

        String userID = credential.getNameID().getValue();

        String givenName = credential.getAttributeAsString(attrFirstName);
        String surname = credential.getAttributeAsString(attrLastName);
        String email = credential.getAttributeAsString(attrEmail);

        UserDetails userDetails = null;
        try {
            userDetails = loadUserByUsername(userID);

        } catch (UsernameNotFoundException e) {
            throw e;
            // TODO fix deny access nicely
        }

        return userDetails;
    }

    public MyUserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        return new MyUserDetails(user);
    }

    @Transactional
    public boolean addUser(User user) {
        userRepository.save(user);
        return true;
    }

    @Transactional
    public boolean deleteUser(User user) {
        userRepository.delete(user);
        return true;
    }
}


