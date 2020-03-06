package uk.cam.lib.cdl.loading.forms;

import javax.validation.constraints.NotBlank;

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

    private boolean isEnabled = true;

    public UserForm() {

    }

    public UserForm(long id, @NotBlank(message = "Must specify a user name (nameID).") String username,
                    String password, @NotBlank(message = "Must specify a first name.") String firstName,
                    @NotBlank(message = "Must specify a last name.") String lastName,
                    @NotBlank(message = "Must specify an email") String email, boolean isEnabled) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.isEnabled = isEnabled;
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

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}

