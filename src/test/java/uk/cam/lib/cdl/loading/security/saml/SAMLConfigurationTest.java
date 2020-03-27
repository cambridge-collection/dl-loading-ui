package uk.cam.lib.cdl.loading.security.saml;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.security.web.FilterChainProxy;
import uk.cam.lib.cdl.loading.security.UsersConfig;

import javax.servlet.Filter;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.cam.lib.cdl.loading.security.saml.SAMLTestUtils.samlCredentialWithID;

@SpringBootTest(properties = {
    "auth.methods=saml"
})
public class SAMLConfigurationTest {
    @Autowired
    public ApplicationContext applicationContext;

    @Autowired
    @Qualifier(SAMLConfig.SAML_FILTER_NAME)
    public Filter samlAuthFilter;

    @Test
    public void samlAuthFilterIsRegistered() {
        var filterChain = applicationContext.getBean("springSecurityFilterChain", FilterChainProxy.class);

        assertThat(filterChain.getFilters("/")).contains(samlAuthFilter);
    }

    @SpringBootTest(properties = {
            "auth.methods=saml",
            "users.source=test"
    })
    public static class SAMLUserDetailsServiceTest {
        @TestConfiguration
        public static class Config {
            @Bean(UsersConfig.USER_DETAILS_SERVICE)
            public UserDetailsService userDetailsService() {
                return mock(UserDetailsService.class);
            }
        }

        @Autowired
        @Qualifier(UsersConfig.USER_DETAILS_SERVICE)
        private UserDetailsService userDetailsService;

        @Autowired
        private SAMLUserDetailsService samlUserDetailsService;

        @Test
        public void samlUserDetailsServiceBeanUsesUserDetailsServiceFromUsersConfig() {
            var userDetails = mock(UserDetails.class);
            when(userDetailsService.loadUserByUsername(any())).thenReturn(userDetails);

            assertThat(samlUserDetailsService.loadUserBySAML(samlCredentialWithID("example"))).isSameInstanceAs(userDetails);

            verify(userDetailsService, times(1)).loadUserByUsername("example");
        }
    }
}
