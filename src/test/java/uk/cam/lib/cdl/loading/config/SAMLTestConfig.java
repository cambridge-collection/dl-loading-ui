package uk.cam.lib.cdl.loading.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.saml.key.EmptyKeyManager;
import org.springframework.security.saml.key.KeyManager;
import uk.cam.lib.cdl.loading.security.saml.ConditionalOnSAMLAuth;

@Configuration
@ConditionalOnSAMLAuth
public class SAMLTestConfig {
    @Bean
    public KeyManager keyManager() {
        return new EmptyKeyManager();
    }
}
