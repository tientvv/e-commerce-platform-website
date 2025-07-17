package com.tientv.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "accounts", schema = "dbo")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 255)
    @Column(name = "username")
    private String username;

    @Size(max = 255)
    @Column(name = "password")
    private String password;

    @Size(max = 255)
    @Column(name = "email")
    private String email;

    @Size(max = 13)
    @Column(name = "phone", length = 13)
    private String phone;

    @Nationalized
    @Lob
    @Column(name = "address")
    private String address;

    @Size(max = 50)
    @Column(name = "role", length = 50)
    private String role;

    @OneToMany(mappedBy = "account")
    private Set<Order> orders = new LinkedHashSet<>();

    @OneToMany(mappedBy = "account")
    private Set<Review> reviews = new LinkedHashSet<>();

    @OneToMany(mappedBy = "account")
    private Set<Wishlist> wishlists = new LinkedHashSet<>();

}