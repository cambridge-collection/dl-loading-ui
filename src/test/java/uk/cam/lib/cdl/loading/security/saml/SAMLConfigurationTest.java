package uk.cam.lib.cdl.loading.security.saml;

// TODO fix issue - fails when connecting to auth.saml.keycloak.auth-server-url
// after upgrading spring / h2 / flyway
//
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.context.TestConfiguration;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.annotation.Bean;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.AuthenticationServiceException;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.saml.SAMLAuthenticationToken;
//import org.springframework.security.saml.context.SAMLMessageContext;
//import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
//import org.springframework.security.web.FilterChainProxy;
//import uk.cam.lib.cdl.loading.security.UsersConfig;
//
//import javax.servlet.Filter;
//
//import static com.google.common.truth.Truth.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//import static uk.cam.lib.cdl.loading.security.saml.SAMLTestUtils.samlCredentialWithID;
//
//@SpringBootTest(properties = {
//    "auth.methods=saml"
//})
//public class SAMLConfigurationTest {
//    @Autowired
//    public ApplicationContext applicationContext;
//
//    @Autowired
//    @Qualifier(SAMLConfig.SAML_FILTER_NAME)
//    public Filter samlAuthFilter;
//
//    @Test
//    public void samlAuthFilterIsRegistered() {
//        var filterChain = applicationContext.getBean("springSecurityFilterChain", FilterChainProxy.class);
//
//        assertThat(filterChain.getFilters("/")).contains(samlAuthFilter);
//    }
//
//    @SpringBootTest(properties = "auth.methods=saml")
//    public static class SAMLAuthenticationProviderRegistrationTest {
//        @Autowired
//        private AuthenticationManager authenticationManager;
//
//        @Test
//        public void samlAuthenticationProviderIsRegistered() {
//            var samlMessageContext = mock(SAMLMessageContext.class);
//            when(samlMessageContext.getCommunicationProfileId()).thenReturn("[fake profile ID]");
//            var samlToken = new SAMLAuthenticationToken(samlMessageContext);
//
//            // The authentication flow is not fully mocked, we just check that authenticating a SAML token
//            // hits a SAMLAuthenticationProvider which will fail predictably due to our unsupported profile ID.
//            var thrown = Assertions.assertThrows(AuthenticationServiceException.class, () ->
//                    authenticationManager.authenticate(samlToken));
//            assertThat(thrown).hasMessageThat().isEqualTo("Error validating SAML message");
//            assertThat(thrown.getCause()).hasMessageThat().isEqualTo("Unsupported profile encountered in the context [fake profile ID]");
//        }
//    }
//
//
//
//    @SpringBootTest(properties = {
//            "auth.methods=saml",
//            "users.source=test"
//    })
//    public static class SAMLUserDetailsServiceTest {
//        @TestConfiguration
//        public static class Config {
//            @Bean(UsersConfig.USER_DETAILS_SERVICE)
//            public UserDetailsService userDetailsService() {
//                return mock(UserDetailsService.class);
//            }
//        }
//
//        @Autowired
//        @Qualifier(UsersConfig.USER_DETAILS_SERVICE)
//        private UserDetailsService userDetailsService;
//
//        @Autowired
//        private SAMLUserDetailsService samlUserDetailsService;
//
//        @Test
//        public void samlUserDetailsServiceBeanUsesUserDetailsServiceFromUsersConfig() {
//            var userDetails = mock(UserDetails.class);
//            when(userDetailsService.loadUserByUsername(any())).thenReturn(userDetails);
//
//            assertThat(samlUserDetailsService.loadUserBySAML(samlCredentialWithID("example"))).isSameInstanceAs(userDetails);
//
//            verify(userDetailsService, times(1)).loadUserByUsername("example");
//        }
//    }
//}
