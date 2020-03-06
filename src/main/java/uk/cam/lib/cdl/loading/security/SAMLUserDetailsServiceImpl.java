package uk.cam.lib.cdl.loading.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.stereotype.Service;
import uk.cam.lib.cdl.loading.dao.UserRepository;
import uk.cam.lib.cdl.loading.model.RolesPrefix;
import uk.cam.lib.cdl.loading.model.security.Role;
import uk.cam.lib.cdl.loading.model.security.User;

import javax.transaction.Transactional;
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

    @Autowired
    private UserRepository userRepository;

    public UserDetails loadUserBySAML(SAMLCredential credential) {

        String userID = credential.getNameID().getValue();

        String givenName = credential.getAttributeAsString(attrFirstName);
        String surname = credential.getAttributeAsString(attrLastName);
        String email = credential.getAttributeAsString(attrEmail);

        UserDetails userDetails = null;
        try {
            userDetails = loadUserByUsername(userID);

        } catch (UsernameNotFoundException e) {

            //TODO deny access
            User user = new User();
            user.setUsername(userID);
            user.setEmail(email);
            user.setFirstName(givenName);
            user.setLastName(surname);
            user.setPassword("");
            user.setEnabled(true);

            // Give everyone UNKNOWN ROLE when they log in;
            List<Role> authorities = new ArrayList<>();
            Role role = new Role();
            role.setUser(user);
            role.setAuthority("ROLE_UNKNOWN");
            authorities.add(role);
            user.setAuthorities(authorities);

            boolean success = addUser(user);
            if (!success) {
                throw new SecurityException("Problem adding user to database. ");
            }

            userDetails = new MyUserDetails(user);
        }


        // TODO Allow the recording of roles for specific users
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


