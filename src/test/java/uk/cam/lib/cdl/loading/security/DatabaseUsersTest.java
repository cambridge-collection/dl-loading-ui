/*
package uk.cam.lib.cdl.loading.security;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import uk.cam.lib.cdl.loading.dao.UserRepository;
import uk.cam.lib.cdl.loading.model.security.User;

import static com.google.common.truth.Truth.assertThat;

@SpringBootTest(properties = {
    "users.source=database"
})
public class DatabaseUsersTest {

    @Autowired
    public AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setupUsers() {

        userRepository.deleteAll();

        User user1 = new User();
        user1.setUsername("User1");
        user1.setFirstName("User1_First");
        user1.setLastName("User1_Last");
        user1.setEmail("user1@emailaddress.com");
        user1.setPassword("password1");
        user1.setEnabled(true);

        User user2 = new User();
        user2.setUsername("User2");
        user2.setFirstName("User2_First");
        user2.setLastName("User2_Last");
        user2.setEmail("user2@emailaddress.com");
        user2.setPassword("password2");
        user2.setEnabled(false);

        User user3 = new User();
        user3.setUsername("User3");
        user3.setFirstName("User3_First");
        user3.setLastName("User3_Last");
        user3.setEmail("user3@emailaddress.com");
        user3.setPassword("password3");
        user3.setEnabled(true);

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

    }

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

}
*/
