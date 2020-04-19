package uk.cam.lib.cdl.loading.security.basic;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import static com.google.common.truth.Truth.assertThat;

@SpringBootTest(properties = {
    "auth.methods=basic"
})
public class BasicAuthTest {
    @Autowired
    public ApplicationContext applicationContext;

    @Test
    public void basicAuthFilterIsRegistered() {
        var filterChain = applicationContext.getBean("springSecurityFilterChain", FilterChainProxy.class);

        assertThat(filterChain.getFilters("/").stream().anyMatch(BasicAuthenticationFilter.class::isInstance))
            .isTrue();
    }
}
