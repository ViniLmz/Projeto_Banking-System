
package com.bank_project.user;

import com.bank_project.custumer.Custumer;
import jakarta.persistence.*;

@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @OneToOne
    @JoinColumn(name = "customer_id", unique = true)
    private Custumer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    public AppUser() {
    }


    public AppUser(String username, String password, Custumer customer,UserRole role) {
        this.username = username;
        this.password = password;
        this.customer = customer;
        this.role = role;
    }

    public UserRole getRole() {
        return role;
    }

    public Long getId(){
        return id;
    }

    public void setId(Long id) {
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

    public Custumer getCustomer() {
        return customer;
    }

    public void setCustomer(Custumer customer) {
        this.customer = customer;
    }
}
