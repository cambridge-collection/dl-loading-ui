package uk.cam.lib.cdl.loading.security.basic;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import uk.cam.lib.cdl.loading.security.WebSecurityConfig;

@Configuration
@ConditionalOnBasicAuthentication
public class BasicAuthenticationConfig {

    @Bean
    public BasicAuthenticationHttpSecurityConfigurer basicAuthenticationWebSecurityConfigurer() {
        return new BasicAuthenticationHttpSecurityConfigurer();
    }

    @Qualifier(WebSecurityConfig.QUALIFIER_HTTP_SUB_CONFIGURER)
    @Order(0)
    public static class BasicAuthenticationHttpSecurityConfigurer extends
        AbstractHttpConfigurer<BasicAuthenticationHttpSecurityConfigurer, HttpSecurity> {

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http.httpBasic();
        }
    }
}
