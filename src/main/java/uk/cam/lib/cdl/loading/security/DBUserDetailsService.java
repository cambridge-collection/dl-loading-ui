package uk.cam.lib.cdl.loading.security;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import uk.cam.lib.cdl.loading.dao.UserRepository;
import uk.cam.lib.cdl.loading.model.security.User;

import static com.google.common.base.Preconditions.checkNotNull;

public class DBUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public DBUserDetailsService(UserRepository userRepository) {
        this.userRepository = checkNotNull(userRepository, "userRepository was null");
    }

    @Override
    public MyUserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("No user exists with username: " + username);
        }
        var userDetails = new MyUserDetails(user);

        // The UserDetailsService contract requires we throw if a user has no authorities
        if(userDetails.getAuthorities().isEmpty()) {
            throw new UsernameNotFoundException("User has no granted authorities: " + username);
        }
        return userDetails;
    }
}
