package uk.cam.lib.cdl.loading.model.security;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "Role")
@Table(name = "authorities")
public class Role implements Serializable {

    private static final long serialVersionUID = 5049309976642437603L;

    @Id
    @ManyToOne
    @JoinColumn(name ="id")
    private User user;

    @Id
    @Column(nullable = false, name = "authority")
    private String authority;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }
}
