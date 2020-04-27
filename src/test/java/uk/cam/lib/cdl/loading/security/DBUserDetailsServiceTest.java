package uk.cam.lib.cdl.loading.security;

import com.google.common.collect.ImmutableList;
import com.google.common.truth.Truth;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import uk.cam.lib.cdl.loading.dao.UserRepository;
import uk.cam.lib.cdl.loading.model.security.User;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DBUserDetailsServiceTest {
    @Mock
    private UserRepository userRepository;

    @Test
    public void loadUserByUsernameLoadsExistingUser() {
        var user = new User();
        user.setAuthorities(ImmutableList.of("ROLE_USER"));
        when(userRepository.findByUsername("example")).thenReturn(user);

        var userDetails = new DBUserDetailsService(userRepository).loadUserByUsername("example");

        assertThat(userDetails.getUser()).isSameInstanceAs(user);
    }

    @Test
    public void loadUserByUsernameThrowsForMissingUsers() {
        var userDetailsService = new DBUserDetailsService(userRepository);

        Truth.assertThat(Assertions.assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("example")))
            .hasMessageThat().isEqualTo("No user exists with username: example");
    }

    @Test
    public void loadUserByUsernameThrowsForUsersWithoutAuthorities() {
        var user = new User();
        when(userRepository.findByUsername("example")).thenReturn(user);

        var userDetailsService = new DBUserDetailsService(userRepository);

        Truth.assertThat(Assertions.assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("example")))
                .hasMessageThat().isEqualTo("User has no granted authorities: example");
    }
}
