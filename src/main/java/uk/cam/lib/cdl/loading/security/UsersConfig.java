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
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Component;

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

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    @ConditionalOnExpression("@environment.getProperty('users.source') == 'database'")
    public @interface ConditionalOnDatabaseUsersSource { }

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
            PasswordEncoder passwordEncoder) {
            // Use InMemoryUserDetailsManager to parse the user properties into user objects
            var udm = new InMemoryUserDetailsManager(hardcodedUsersProperties);

            return hardcodedUsersProperties.keySet().stream().map(username -> (String)username).map(username -> {
                if(!username.toLowerCase().equals(username)) {
                    throw new IllegalArgumentException("Hardcoded usernames must be lower case; got: " + username);
                }

                var user = udm.loadUserByUsername(username);

                if(passwordEncoder.upgradeEncoding(user.getPassword())) {
                    user = new User(
                        user.getUsername(), passwordEncoder.encode(user.getPassword()), user.isEnabled(),
                        user.isAccountNonExpired(), user.isCredentialsNonExpired(), user.isAccountNonLocked(),
                        user.getAuthorities());
                }

                return user;
            }).collect(ImmutableMap.toImmutableMap(UserDetails::getUsername, u -> u));
        }

        /**
         * The UserDetailsService for authn/authz - backed by a fixed list of users from a config file.
         */
        @Bean(name = {QUALIFIER + "#userDetailsManager", QUALIFIER})
        @ConditionalOnExpression("!(@environment.getProperty('users.hardcoded-users-file', '').isEmpty())")
        @Qualifier(QUALIFIER)
        public InMemoryUserDetailsManager userDetailsManager(@Qualifier(HARDCODED_USERS) Map<String, UserDetails> hardcodedUsers) {
            return new InMemoryUserDetailsManager(hardcodedUsers.values());
        }

        @Bean(name = {QUALIFIER + "#emptyUserDetailsManager", QUALIFIER})
        @ConditionalOnExpression("@environment.getProperty('users.hardcoded-users-file', '') == ''")
        public InMemoryUserDetailsManager emptyUserDetailsManager() {
            LOG.warn("Property users.hardcoded-users-file not set, no users will be defined");
            return new InMemoryUserDetailsManager();
        }

        @Component
        @ConditionalOnHardcodedUsersSource
        @Qualifier(WebSecurityConfig.QUALIFIER_AUTH_SUB_CONFIGURER)
        public static class HardcodedUsersAuthSecurityConfigurer
            extends SecurityConfigurerAdapter<AuthenticationManager, AuthenticationManagerBuilder> {

            private final InMemoryUserDetailsManager inMemoryUserDetailsManager;
            private final PasswordEncoder passwordEncoder;

            public HardcodedUsersAuthSecurityConfigurer(
                @Qualifier(QUALIFIER) InMemoryUserDetailsManager inMemoryUserDetailsManager,
                PasswordEncoder passwordEncoder
            ) {
                this.inMemoryUserDetailsManager = checkNotNull(inMemoryUserDetailsManager);
                this.passwordEncoder = checkNotNull(passwordEncoder);
            }

            @Override
            public void configure(AuthenticationManagerBuilder auth) throws Exception {
                auth.userDetailsService(this.inMemoryUserDetailsManager)
                    .passwordEncoder(this.passwordEncoder);
            }
        }
    }

    @ConditionalOnDatabaseUsersSource
    public static class DatabaseUsersConfig {
        private static final Logger LOG = LoggerFactory.getLogger(DatabaseUsersConfig.class);
        private static final String QUALIFIER = "uk.cam.lib.cdl.loading.security.UsersConfig.DatabaseUsersConfig";

        @Bean(name = {QUALIFIER+"#DBUserDetailsService", QUALIFIER})
        public UserDetailsService dbUserDetailsService() {
            return new DBUserDetailsService();
        }

        @Component
        @ConditionalOnDatabaseUsersSource
        @Qualifier(WebSecurityConfig.QUALIFIER_AUTH_SUB_CONFIGURER)
        public static class DatabaseUsersAuthSecurityConfigurer
            extends SecurityConfigurerAdapter<AuthenticationManager, AuthenticationManagerBuilder> {

            private final UserDetailsService userDetailsService;

            public DatabaseUsersAuthSecurityConfigurer(
                @Qualifier(QUALIFIER + "#DBUserDetailsService") UserDetailsService userDetailsService
            ) {
                this.userDetailsService = userDetailsService;
            }

            @Override
            public void configure(AuthenticationManagerBuilder auth) throws Exception {

                auth.userDetailsService(this.userDetailsService);
            }
        }
    }
}
