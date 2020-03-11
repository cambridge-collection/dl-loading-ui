package uk.cam.lib.cdl.loading.security.saml;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import uk.cam.lib.cdl.loading.security.WebSecurityConfig;

import javax.servlet.Filter;

import static com.google.common.truth.Truth.assertThat;

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
}
