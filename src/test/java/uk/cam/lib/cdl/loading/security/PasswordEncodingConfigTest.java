package uk.cam.lib.cdl.loading.security;

import com.google.common.truth.Truth;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordEncodingConfigTest {

    @SpringBootTest(properties = {
        "auth.password-encoding.method=insecure-plaintext-for-testing",
    })
    public static class InsecurePlaintextPasswordEncodingConfigTest {
        @Autowired
        public PasswordEncoder passwordEncoder;

        @Test
        public void noopPasswordEncoderIsInjected() {
            Truth.assertThat(passwordEncoder.encode("foo")).isEqualTo("foo");
            Truth.assertThat(passwordEncoder.upgradeEncoding("foo")).isFalse();
        }
    }

    @SpringBootTest(properties = {
        "auth.password-encoding.method=default",
    })
    public static class DefaultPasswordEncodingConfigTest {
        @Autowired
        public PasswordEncoder passwordEncoder;

        @Test
        public void defaultPasswordEncoderIsInjected() {
            Truth.assertThat(passwordEncoder).isInstanceOf(DelegatingPasswordEncoder.class);
            Truth.assertThat(passwordEncoder.encode("foo")).startsWith("{bcrypt}");
            Truth.assertThat(passwordEncoder.upgradeEncoding("foo")).isTrue();
        }
    }

    @SpringBootTest(properties = {})
    public static class UnconfiguredPasswordEncodingConfigTest extends DefaultPasswordEncodingConfigTest {
        @Autowired
        public Environment environment;

        @Test
        public void noPasswordEncoderIsConfigured() {
            Truth.assertThat(environment.containsProperty("auth.password-encoding.method")).isFalse();
        }
    }
}
