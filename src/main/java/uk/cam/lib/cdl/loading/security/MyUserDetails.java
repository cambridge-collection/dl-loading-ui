package uk.cam.lib.cdl.loading.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import uk.cam.lib.cdl.loading.model.security.User;

import java.util.Collection;
import java.util.HashSet;

public class MyUserDetails implements UserDetails {

    private static final long serialVersionUID = -4868816810054682474L;
    private User user;
    private Collection<GrantedAuthority> authorities  = new HashSet<>();;

    public MyUserDetails(User user) {
        this.user = user;
        initAuthorities(user);
    }

    public User getUser() {
        return user;
    }

    private void initAuthorities(User user) {
        if (user.getAuthorities() == null) {
            return;
        }
        for (String authority : user.getAuthorities()) {
            authorities.add(new SimpleGrantedAuthority(authority));
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }
}
