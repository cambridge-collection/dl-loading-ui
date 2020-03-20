package uk.cam.lib.cdl.loading.security;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import uk.cam.lib.cdl.loading.security.basic.BasicUserDetailsServiceImpl;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

@Configuration
public class UsersConfig {

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    @ConditionalOnExpression("@environment.getProperty('users.source') == 'hardcoded' || !@environment.containsProperty('users.source')")
    public @interface ConditionalOnHardcodedUsersSource { }

    @ConditionalOnHardcodedUsersSource
    public static class HardcodedUsersConfig {
        private static final Logger LOG = LoggerFactory.getLogger(HardcodedUsersConfig.class);
        private static final String QUALIFIER = "uk.cam.lib.cdl.loading.security.UsersConfig.HardcodedUsersConfig";

        static final String HARDCODED_USERS = "uk.cam.lib.cdl.loading.security.UsersConfig#hardcoded-users";

        @Bean
        @Lazy
        public Properties hardcodedUsersProperties(@Value("${users.hardcoded-users-file}") Path hardcodedUsersFile) throws IOException {
            Properties properties = new Properties();
            properties.load(Files.newBufferedReader(hardcodedUsersFile));
            return properties;
        }

        @Bean(HARDCODED_USERS)
        @Lazy
        public Map<String, UserDetails> hardcodedUsers(
            @Qualifier("hardcodedUsersProperties") Properties hardcodedUsersProperties,
            @Qualifier(QUALIFIER + "#userDetailsService") UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

            return hardcodedUsersProperties.keySet().stream().map(username -> (String)username).map(username -> {
                if(!username.toLowerCase().equals(username)) {
                    throw new IllegalArgumentException("Hardcoded usernames must be lower case; got: " + username);
                }

                UserDetails user = userDetailsService.loadUserByUsername(username);

                if(passwordEncoder.upgradeEncoding(user.getPassword())) {
                    user = new User(
                        user.getUsername(), passwordEncoder.encode(user.getPassword()), user.isEnabled(),
                        user.isAccountNonExpired(), user.isCredentialsNonExpired(), user.isAccountNonLocked(),
                        user.getAuthorities());
                }

                return user;
            }).collect(ImmutableMap.toImmutableMap(UserDetails::getUsername, u -> u));
        }

        @Bean(name = {QUALIFIER + "#userDetailsService", QUALIFIER})
        @Qualifier(QUALIFIER)
        public UserDetailsService basicUserDetailsService() {
            return new BasicUserDetailsServiceImpl();
        }

        @Component
        @ConditionalOnHardcodedUsersSource
        @Qualifier(WebSecurityConfig.QUALIFIER_AUTH_SUB_CONFIGURER)
        public static class HardcodedUsersAuthSecurityConfigurer
            extends SecurityConfigurerAdapter<AuthenticationManager, AuthenticationManagerBuilder> {

           // private final InMemoryUserDetailsManager inMemoryUserDetailsManager;
            private final PasswordEncoder passwordEncoder;
            private final UserDetailsService userDetailsService;

            public HardcodedUsersAuthSecurityConfigurer(
                @Qualifier(QUALIFIER) UserDetailsService userDetailsService,
                PasswordEncoder passwordEncoder
            ) {
               // this.inMemoryUserDetailsManager = checkNotNull(inMemoryUserDetailsManager);
                this.passwordEncoder = checkNotNull(passwordEncoder);
                this.userDetailsService = userDetailsService;
            }

            @Override
            public void configure(AuthenticationManagerBuilder auth) throws Exception {
                auth.userDetailsService(this.userDetailsService)
                    .passwordEncoder(this.passwordEncoder);
            }
        }
    }
}
