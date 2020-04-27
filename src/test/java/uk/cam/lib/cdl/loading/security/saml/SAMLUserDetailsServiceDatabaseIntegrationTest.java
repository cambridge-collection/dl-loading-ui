package uk.cam.lib.cdl.loading.security.saml;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml2.core.NameID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.test.annotation.DirtiesContext;
import uk.cam.lib.cdl.loading.dao.UserRepository;
import uk.cam.lib.cdl.loading.model.security.User;
import uk.cam.lib.cdl.loading.security.MyUserDetails;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.cam.lib.cdl.loading.security.saml.SAMLTestUtils.samlCredentialWithID;

/**
 * An integration test to verify that SAML authentiation is able to locate users
 * stored in the database when users.source is database.
 */
@SpringBootTest(properties = {
    "auth.methods=saml",
    "users.source=database"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SAMLUserDetailsServiceDatabaseIntegrationTest {
    @Autowired
    private SAMLUserDetailsService samlUserDetailsService;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    private void generateUsers() {
        // Generate a fake user to be retrieved
        user = new User();
        user.setUsername("example");
        user.setPassword("");
        user.setFirstName("Foo");
        user.setLastName("");
        user.setEmail("");
        user.setAuthorities(ImmutableList.of("ROLE_USER"));

        userRepository.save(user);
    }

    @Test
    public void samlUserDetailsServiceKnowsAboutUsersInDatabase() {
        var result = samlUserDetailsService.loadUserBySAML(samlCredentialWithID("example"));

        assertThat(result).isInstanceOf(MyUserDetails.class);
        var userDetails = (MyUserDetails)result;
        assertThat(userDetails.getUser().getUsername()).isEqualTo("example");
        assertThat(userDetails.getUser().getFirstName()).isEqualTo("Foo");
    }
}
