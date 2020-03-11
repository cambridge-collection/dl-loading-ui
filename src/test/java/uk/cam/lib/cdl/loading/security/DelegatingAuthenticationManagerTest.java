package uk.cam.lib.cdl.loading.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class DelegatingAuthenticationManagerTest {
    @Test
    public void setParentCanOnlyBeUsedOnce() {
        var m = new DelegatingAuthenticationManager();
        m.setParent(mock(AuthenticationManager.class, "INITIAL"));
        assertThat(assertThrows(IllegalStateException.class, () -> m.setParent(mock(AuthenticationManager.class))))
            .hasMessageThat().matches("^uk.cam.lib.cdl.loading.security.DelegatingAuthenticationManager@\\w+ is already initialised with parent: INITIAL$");
    }

    @Test
    public void authenticateThrowsIfParentIsNotSet() {
        var m = new DelegatingAuthenticationManager();
        assertThat(assertThrows(IllegalStateException.class, () -> m.authenticate(mock(Authentication.class))))
            .hasMessageThat().matches("^authenticate\\(\\) called on uk.cam.lib.cdl.loading.security.DelegatingAuthenticationManager@\\w+ before it was configured to delegate to a real AuthenticationManager$");
    }

    @Test
    public void authenticateDelegatesToParent() {
        var authentication = mock(Authentication.class);
        var parent = mock(AuthenticationManager.class);
        doReturn(authentication).when(parent).authenticate(authentication);

        var delegatingManager = new DelegatingAuthenticationManager();
        delegatingManager.setParent(parent);
        var result = delegatingManager.authenticate(authentication);

        assertThat(result).isSameInstanceAs(authentication);
        verify(parent, times(1)).authenticate(authentication);
    }
}
