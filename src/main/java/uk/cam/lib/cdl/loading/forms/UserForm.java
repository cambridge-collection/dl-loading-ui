package uk.cam.lib.cdl.loading.forms;

import org.springframework.security.crypto.password.PasswordEncoder;
import uk.cam.lib.cdl.loading.model.security.User;

import javax.validation.constraints.NotBlank;
import java.util.List;

public class UserForm {

    private long id;

    @NotBlank(message = "Must specify a user name (nameID).")
    private String username;
    private String password;

    @NotBlank(message = "Must specify a first name.")
    private String firstName;

    @NotBlank(message = "Must specify a last name.")
    private String lastName;

    @NotBlank(message = "Must specify an email")
    private String email;

    private boolean enabled;
    private List<String> authorities;

    public UserForm() {

    }

    public UserForm(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.enabled = user.isEnabled();
        this.authorities = user.getAuthorities();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(List<String> authorities) {
        this.authorities = authorities;
    }

    public User toUser(PasswordEncoder passwordEncoder) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        if (password ==null || "".equals(password.trim())) {
            user.setPassword(null);
        } else {
            user.setPassword(passwordEncoder.encode(password));
        }
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(enabled);
        user.setAuthorities(authorities);
        user.setEmail(email);

        return user;
    }
}

