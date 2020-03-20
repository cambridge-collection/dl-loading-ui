package uk.cam.lib.cdl.loading.security;

import com.google.common.collect.ImmutableList;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.Aware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.SecurityConfigurer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import java.util.List;
import java.util.Optional;

/**
 * This is the primary root of the Spring Security configuration.
 *
 * <p>The {@link WebSecurityConfigurer} bean it creates configures Spring Security by invoking small, targeted
 * sub-configurers, such as {@link uk.cam.lib.cdl.loading.security.basic.BasicAuthenticationConfig.BasicAuthenticationWebSecurityConfigurer}
 * to activate specific functionality based on properties set via Spring configuration.
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class WebSecurityConfig {

    public static final String AUTHENTICATION_MANAGER = "uk.cam.lib.cdl.loading.security.WebSecurityConfig#authenticationManager";
    private static final String REAL_AUTHENTICATION_MANAGER = "uk.cam.lib.cdl.loading.security.WebSecurityConfig#realAuthenticationManager";

    /**
     * An AuthenticationManager which delegates to the actual manager created at some future point by the Spring
     * Security configuration machinery.
     *
     * <p>Some beans require a reference to an AuthenticationManager at the time of their creation, but this typically
     * forms a dependency cycle if such beans are also used by the Spring Security configuration builders, as the
     * builders create the AuthenticationManager.
     *
     * <p>The manager returned here initially does nothing, but is later hooked up to delegate to the real auth manager.
     */
    @Qualifier
    @Bean(name = AUTHENTICATION_MANAGER)
    @Primary
    public AuthenticationManager authenticationManagerProxy() {
        return new DelegatingAuthenticationManager();
    }

    /**
     * Beans implementing this interface will be notified of the AuthenticationManager used by the dl-loader-ui app.
     */
    interface AuthenticationManagerAware extends Aware {
        void setAuthenticationManager(AuthenticationManager authenticationManager);
    }

    /**
     * Responsible for initialising the {@link #authenticationManagerProxy()} bean with the AuthenticationManager
     * created by the Spring Security builder when it becomes available.
     */
    @Bean
    public AuthenticationManagerAware authenticationManagerProxyInitialiser(
        @Qualifier(AUTHENTICATION_MANAGER) AuthenticationManager authenticationManagerProxy
    ) {
        assert authenticationManagerProxy instanceof DelegatingAuthenticationManager;
        var _authenticationManagerProxy = (DelegatingAuthenticationManager)authenticationManagerProxy;
        return _authenticationManagerProxy::setParent;
    }

    /**
     * Responsible for listening for the Spring Security AuthenticationManager becoming available, and notifies all
     * defined {@link AuthenticationManagerAware} beans.
     */
    @Bean
    public BeanPostProcessor authenticationManagerAwareInvoker(ListableBeanFactory lbf) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if(REAL_AUTHENTICATION_MANAGER.equals(beanName) && bean instanceof AuthenticationManager) {
                    var authenticationManager = (AuthenticationManager)bean;
                    lbf.getBeansOfType(AuthenticationManagerAware.class).values()
                        .forEach(ama -> ama.setAuthenticationManager(authenticationManager));
                }
                return bean;
            }
        };
    }

    /** The primary WebSecurityConfigurerAdapter responsible for kicking off all Spring Security configuration. */
    @Bean
    public WebSecurityConfigurer webSecurityConfigurer(
        @Qualifier(QUALIFIER_HTTP_SUB_CONFIGURER) Optional<List<SecurityConfigurer<DefaultSecurityFilterChain, HttpSecurity>>> httpSecurityConfigurerSubConfigurers,
        @Qualifier(QUALIFIER_AUTH_SUB_CONFIGURER) Optional<List<SecurityConfigurer<AuthenticationManager, AuthenticationManagerBuilder>>> authenticationManagerBuilderSubConfigurers,
        @Qualifier(QUALIFIER_WEB_SUB_CONFIGURER) Optional<List<SecurityConfigurer<Filter, WebSecurity>>> webSecuritySubConfigurers
    ) {
        return new WebSecurityConfigurer(
            httpSecurityConfigurerSubConfigurers.orElseGet(ImmutableList::of),
            authenticationManagerBuilderSubConfigurers.orElseGet(ImmutableList::of),
            webSecuritySubConfigurers.orElseGet(ImmutableList::of));
    }

    /** Spring Security's configured AuthenticationManager. */
    @Bean(value = REAL_AUTHENTICATION_MANAGER, autowireCandidate = false)
    public AuthenticationManager webSecurityConfigurerAuthenticationManager(WebSecurityConfigurer webSecurityConfigurer) throws Exception {
        return webSecurityConfigurer.authenticationManagerBean();
    }

    /**
     * A value for {@link org.springframework.beans.factory.annotation.Qualifier} annotations marking objects
     * implementing {@code SecurityConfigurer<DefaultSecurityFilterChain, HttpSecurity>} to be used by
     * {@link WebSecurityConfigurer} to perform additional (optional/pluggable) configuration of its
     * {@link HttpSecurity} object.
     */
    public static final String QUALIFIER_HTTP_SUB_CONFIGURER = "uk.cam.lib.cdl.loading.security.WebSecurityConfig#httpSecurityConfigurerSubConfigurer";
    public static final String QUALIFIER_AUTH_SUB_CONFIGURER = "uk.cam.lib.cdl.loading.security.WebSecurityConfig#authenticationManagerBuilderSubConfigurer";
    public static final String QUALIFIER_WEB_SUB_CONFIGURER = "uk.cam.lib.cdl.loading.security.WebSecurityConfig#webSecuritySubConfigurer";

    /** Configures the baseline/default Spring Security behaviour which should always be active. */
    @Component
    @Qualifier(WebSecurityConfig.QUALIFIER_HTTP_SUB_CONFIGURER)
    public static class DefaultsWebSecurityConfigurer extends
        AbstractHttpConfigurer<DefaultsWebSecurityConfigurer, HttpSecurity> {

        @Override
        public void configure(HttpSecurity http) throws Exception {
            // FIXME: CSRF protection shouldn't be disabled
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

    /** A {@link WebSecurityConfigurerAdapter} which does nothing itself except delegate to any available sub configurers. */
    public static class WebSecurityConfigurer extends WebSecurityConfigurerAdapter {
        private final List<SecurityConfigurer<DefaultSecurityFilterChain, HttpSecurity>> httpSecuritySubConfigurers;
        private final List<SecurityConfigurer<AuthenticationManager, AuthenticationManagerBuilder>> authenticationManagerBuilderSubConfigurers;
        private final List<SecurityConfigurer<Filter, WebSecurity>> webSecuritySubConfigurers;

        public WebSecurityConfigurer(
            Iterable<SecurityConfigurer<DefaultSecurityFilterChain, HttpSecurity>> httpSecurityConfigurers,
            List<SecurityConfigurer<AuthenticationManager, AuthenticationManagerBuilder>> authenticationManagerBuilderSubConfigurers,
            List<SecurityConfigurer<Filter, WebSecurity>> webSecuritySubConfigurers
        ) {
            this.httpSecuritySubConfigurers = ImmutableList.copyOf(httpSecurityConfigurers);
            this.authenticationManagerBuilderSubConfigurers = ImmutableList.copyOf(authenticationManagerBuilderSubConfigurers);
            this.webSecuritySubConfigurers = ImmutableList.copyOf(webSecuritySubConfigurers);
        }

        @Override
        public void init(WebSecurity web) throws Exception {
            super.init(web);

            for (SecurityConfigurer<Filter, WebSecurity> subConfigurer : this.webSecuritySubConfigurers) {
                subConfigurer.init(web);
            }
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            if(this.authenticationManagerBuilderSubConfigurers.isEmpty()) {
                super.configure(auth);
            }
            else {
                this.initSubConfigurers(auth);
                this.applySubConfigurers(auth);
            }
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            if(this.httpSecuritySubConfigurers.isEmpty()) {
                super.configure(http);
            }
            else {
                this.initSubConfigurers(http);
                this.applySubConfigurers(http);
            }
        }

        private void initSubConfigurers(HttpSecurity http) throws Exception {
            for (SecurityConfigurer<DefaultSecurityFilterChain, HttpSecurity> subConfigurer : this.httpSecuritySubConfigurers) {
                subConfigurer.init(http);
            }
        }
        private void initSubConfigurers(AuthenticationManagerBuilder auth) throws Exception {
            for (SecurityConfigurer<AuthenticationManager, AuthenticationManagerBuilder> subConfigurer : this.authenticationManagerBuilderSubConfigurers) {
                subConfigurer.init(auth);
            }
        }
        private void applySubConfigurers(HttpSecurity http) throws Exception {
            for (SecurityConfigurer<DefaultSecurityFilterChain, HttpSecurity> subConfigurer : this.httpSecuritySubConfigurers) {
                subConfigurer.configure(http);
            }
        }
        private void applySubConfigurers(AuthenticationManagerBuilder auth) throws Exception {
            for (SecurityConfigurer<AuthenticationManager, AuthenticationManagerBuilder> subConfigurer : this.authenticationManagerBuilderSubConfigurers) {
                subConfigurer.configure(auth);
            }
        }
    }
}
