package uk.cam.lib.cdl.loading.forms;

import uk.cam.lib.cdl.loading.model.security.User;

import javax.validation.constraints.NotBlank;
import java.util.List;

public class UserForm {

    private User user;

    public UserForm() {
        this.user = new User();
    }

    public UserForm(User user) {
        this.user = user;
    }

    public long getId() {
        return user.getId();
    }

    public void setId(long id) {
        user.setId(id);
    }

    public String getUsername() {
        return user.getUsername();
    }

    public void setUsername(@NotBlank(message = "Must specify a user name (nameID).") String username) {
        user.setUsername(username);
    }

    public String getPassword() {
        return user.getPassword();
    }

    public void setPassword(String password) {
        user.setPassword(password);
    }

    public String getFirstName() {
        return user.getFirstName();
    }

    public void setFirstName(@NotBlank(message = "Must specify a first name.") String firstName) {
        user.setFirstName(firstName);
    }

    public String getLastName() {
        return user.getLastName();
    }

    public void setLastName(@NotBlank(message = "Must specify a last name.") String lastName) {
        user.setLastName(lastName);
    }

    public String getEmail() {
        return user.getEmail();
    }

    public void setEmail(@NotBlank(message = "Must specify an email") String email) {
        user.setEmail(email);
    }

    public boolean getEnabled() {
        return user.isEnabled();
    }

    public void setEnabled(boolean enabled) {
        user.setEnabled(enabled);
    }

    public List<String> getAuthorities() {
        return user.getAuthorities();
    }

    public void setAuthorities(List<String> authorities) {
        user.setAuthorities(authorities);
    }

    public User toUser() {
        return user;
    }
}

