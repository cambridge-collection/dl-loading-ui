package uk.cam.lib.cdl.loading.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncodingConfig {

    private static final class NoopPasswordEncoder implements PasswordEncoder {
        @Override
        public String encode(CharSequence rawPassword) {
            return "" + rawPassword;
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            return rawPassword.equals(encodedPassword);
        }
    }

    @Bean
    @ConditionalOnProperty(name = "auth.password-encoding.method", havingValue = "insecure-plaintext-for-testing")
    public PasswordEncoder plaintextNoopPasswordEncoder() {
        return new NoopPasswordEncoder();

    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "auth.password-encoding.method", havingValue = "default", matchIfMissing = true)
    public PasswordEncoder defaultPasswordEncoder() {
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        return passwordEncoder;
    }
}
