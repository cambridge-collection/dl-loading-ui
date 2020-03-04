package uk.cam.lib.cdl.loading.security.saml;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;

import static com.google.common.truth.Truth.assertThat;

@SpringBootTest(properties = {
    "auth.saml.enabled=true"
})
public class SAMLConfigurationTest {
    @Autowired
    private SAMLConfig.SAMLConfigurer samlWebSecurityConfigurer;

    @Autowired
    private SAMLUserDetailsService samlUserDetailsService;

    @Test
    public void samlConfigIsAvailable() {
        assertThat(samlUserDetailsService).isNotNull();
    }

    @Test
    public void samlSecurityConfigurationIsApplied() {
        assertThat(samlWebSecurityConfigurer).isNotNull();
    }
}
