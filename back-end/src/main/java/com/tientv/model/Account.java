package com.tientv.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 255)
    @Column(name = "google_id")
    private String googleId;

    @Size(max = 255)
    @Column(name = "username")
    private String username;

    @Size(max = 255)
    @Nationalized
    @Column(name = "name")
    private String name;

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

    @Lob
    @Column(name = "accounts_image")
    private String accountsImage;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "last_login")
    private OffsetDateTime lastLogin;

    @Size(max = 50)
    @Column(name = "role", length = 50)
    private String role;

    @ColumnDefault("1")
    @Column(name = "is_active")
    private Boolean isActive;

    @OneToMany(mappedBy = "account")
    private Set<Order> orders = new LinkedHashSet<>();

    @OneToMany(mappedBy = "account")
    private Set<ReturnRefund> returnRefunds = new LinkedHashSet<>();

    @OneToMany(mappedBy = "account")
    private Set<Review> reviews = new LinkedHashSet<>();

    @OneToOne(mappedBy = "user")
    private Shop shop;

    @OneToMany(mappedBy = "account")
    private Set<Wishlist> wishlists = new LinkedHashSet<>();

}