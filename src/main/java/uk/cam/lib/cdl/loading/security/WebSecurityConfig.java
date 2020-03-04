package uk.cam.lib.cdl.loading.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class WebSecurityConfig {
    private static final int ROOT_WEB_SECURITY_CONFIGURER_ORDER = 100;
    private static int nextAuthenticationMethodOrder = ROOT_WEB_SECURITY_CONFIGURER_ORDER + 1;
    public static int nextAuthenticationMethodOrder() {
        return nextAuthenticationMethodOrder++;
    }

    /**
     * A WebSecurityConfigurerAdapter which automatically assigns a unique order
     * with lower priority than {@link WebSecurityConfigurer}.
     *
     * This avoids the issue that @Order annotations need to be unique on
     * WebSecurityConfigurers instances, but we don't want them to have to know
     * about each other to choose a unique order.
     */
    public static abstract class AutoOrderedSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter implements Ordered {
        private static int order = Integer.MIN_VALUE;

        @Override
        public int getOrder() {
            if(order == Integer.MIN_VALUE) {
                order = WebSecurityConfig.nextAuthenticationMethodOrder();
            }
            return order;
        }
    }

    @Bean
    @Lazy
    public AuthenticationManager authenticationManager(WebSecurityConfigurer webSecurityConfigurer) throws Exception {
        return webSecurityConfigurer.authenticationManagerBean();
    }

    @Bean
    public WebSecurityConfigurer webSecurityConfigurer() {
        return new WebSecurityConfigurer();
    }

    @Order(ROOT_WEB_SECURITY_CONFIGURER_ORDER)
    public static class WebSecurityConfigurer extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {

            http.httpBasic();

            http.csrf()
                .disable();

            http.authorizeRequests()
                .antMatchers("/js/**").permitAll()
                .antMatchers("/css/**").permitAll()
                .antMatchers("/img/**").permitAll()
                .antMatchers("/webjars/**").permitAll()
                .antMatchers("/login/**").permitAll()
                .anyRequest().authenticated();

            // Required for allowing Iframe embedding from same origin.
            http.headers().frameOptions().disable()
                .addHeaderWriter(new XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN));
        }
    }
}
