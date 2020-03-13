/*
package uk.cam.lib.cdl.loading.model.security;

import javax.persistence.*;
import java.io.Serializable;

*/
/*@Entity(name = "Role")*//*

@Table(name = "authorities")
@Embeddable
public class Role implements Serializable {

    private static final long serialVersionUID = 5049309976642437603L;

*/
/*    @Id
    @ManyToOne
    @JoinColumn(name ="id")*//*

    @Id
    private long id;

*/
/*    @Id
    @Column(nullable = false, name = "authority")*//*

    @Id
    private String authority;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }
}

*/
