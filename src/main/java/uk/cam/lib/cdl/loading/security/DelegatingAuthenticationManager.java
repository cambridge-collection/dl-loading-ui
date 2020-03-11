package uk.cam.lib.cdl.loading.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import static com.google.common.base.Preconditions.checkNotNull;

class DelegatingAuthenticationManager implements AuthenticationManager {

    private AuthenticationManager parent;

    synchronized void setParent(AuthenticationManager parent) {
        if(this.parent != null) {
            throw new IllegalStateException(String.format("%s is already initialised with parent: %s", this, this.parent));
        }
        this.parent = checkNotNull(parent);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        AuthenticationManager parent = this.parent;
        if(parent == null) {
            throw new IllegalStateException(String.format(
                "authenticate() called on %s before it was configured to delegate to a real AuthenticationManager",
                this));
        }
        return parent.authenticate(authentication);
    }

}
