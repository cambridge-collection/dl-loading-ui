package uk.cam.lib.cdl.loading.security;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static uk.cam.lib.cdl.loading.security.UsersConfig.HardcodedUsersConfig.HARDCODED_USERS;

@SpringBootTest(properties = {
    "users.source=hardcoded",
    "users.hardcoded-users-file=#{T(uk.cam.lib.cdl.loading.security.HardcodedUsersTest).getResource('hardcoded-users.properties').getFile()}"
})
public class HardcodedUsersTest {
    @Qualifier(HARDCODED_USERS)
    @Autowired
    public Map<String, UserDetails> hardcodedUsers;

    @Autowired
    public AuthenticationManager authenticationManager;

    @Autowired
    public PasswordEncoder passwordEncoder;

    @ParameterizedTest
    @CsvSource({
        "user1,password1",
        "user3,password3",
    })
    public void authenticationManagerAuthenticatesValidUsers(String user, String pass) {
        var result = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user, pass));
        assertThat(result.isAuthenticated()).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
        "user1,wrong-password,org.springframework.security.authentication.BadCredentialsException", // wrong password
        "user2,password2,org.springframework.security.authentication.DisabledException", // user is disabled
        "user4,password4,org.springframework.security.authentication.BadCredentialsException", // user doesn't exist
    })
    public void authenticationManagerRejectsInvalidUsers(String user, String pass, Class<? extends Exception> expected) {
        Assertions.assertThrows(expected, () ->
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user, pass)));
    }

    @ParameterizedTest
    @CsvSource({
        "user1,password1,true,ROLE_USER",
        "user2,password2,false,ROLE_ADMIN",
        "user3,password3,true,'FOO,BAR,ROLE_USER'"
    })
    public void userPropertiesFileParsing(String username, String password, boolean isEnabled, String authorities) {
        var authoritiesList = StringUtils.commaDelimitedListToSet(authorities);
        assertThat(hardcodedUsers).hasSize(3);
        var user = hardcodedUsers.get(username);
        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(passwordEncoder.matches(password, user.getPassword())).isTrue();
        assertThat(user.isEnabled()).isEqualTo(isEnabled);
        assertThat(user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(ImmutableSet.toImmutableSet()))
            .isEqualTo(authoritiesList);
    }
}
