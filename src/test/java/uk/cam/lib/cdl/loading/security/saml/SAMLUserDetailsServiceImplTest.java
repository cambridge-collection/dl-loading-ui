package uk.cam.lib.cdl.loading.security.saml;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.cam.lib.cdl.loading.security.saml.SAMLTestUtils.samlCredentialWithID;

@ExtendWith(MockitoExtension.class)
public class SAMLUserDetailsServiceImplTest {
    @Mock private UserDetailsService userDetailsService;
    @Mock private UserDetails userDetails;

    private SAMLUserDetailsServiceImpl samlUserDetailsService;

    @BeforeEach
    private void beforeEach() {
        this.samlUserDetailsService = new SAMLUserDetailsServiceImpl(userDetailsService);
    }

    @Test
    public void loadUserBySAMLLoadsUserFromUserDetailsService() {
        when(userDetailsService.loadUserByUsername(any())).thenReturn(userDetails);

        assertThat(samlUserDetailsService.loadUserBySAML(samlCredentialWithID("example")))
                .isSameInstanceAs(userDetails);

        verify(userDetailsService, times(1)).loadUserByUsername("example");
    }

    @Test
    public void loadUserBySAMLThrowsUserNotFoundExceptionFromUserDetailsService() {
        var exc = new UsernameNotFoundException("foo");
        when(userDetailsService.loadUserByUsername(any())).thenThrow(exc);

        var thrown = Assertions.assertThrows(UsernameNotFoundException.class,
                () -> samlUserDetailsService.loadUserBySAML(samlCredentialWithID("example")));
        assertThat(thrown).isSameInstanceAs(exc);
    }
}
