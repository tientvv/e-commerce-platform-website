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
@Table(name = "users")
public class User {
    @Id
    @ColumnDefault("newid()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 255)
    @Nationalized
    @Column(name = "email")
    private String email;

    @Size(max = 255)
    @Nationalized
    @Column(name = "password_hash")
    private String passwordHash;

    @Size(max = 255)
    @Nationalized
    @Column(name = "full_name")
    private String fullName;

    @Size(max = 20)
    @Nationalized
    @Column(name = "phone", length = 20)
    private String phone;

    @Nationalized
    @Lob
    @Column(name = "address")
    private String address;

    @Nationalized
    @Lob
    @Column(name = "avatar_url")
    private String avatarUrl;

    @ColumnDefault("1")
    @Column(name = "is_buyer")
    private Boolean isBuyer;

    @ColumnDefault("0")
    @Column(name = "is_seller")
    private Boolean isSeller;

    @ColumnDefault("0")
    @Column(name = "is_admin")
    private Boolean isAdmin;

    @ColumnDefault("1")
    @Column(name = "is_active")
    private Boolean isActive;

    @ColumnDefault("0")
    @Column(name = "email_verified")
    private Boolean emailVerified;

    @ColumnDefault("0")
    @Column(name = "phone_verified")
    private Boolean phoneVerified;

    @Size(max = 100)
    @Nationalized
    @Column(name = "verification_token", length = 100)
    private String verificationToken;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "sender")
    private Set<Chat> sentChats = new LinkedHashSet<>();

    @OneToMany(mappedBy = "receiver")
    private Set<Chat> receivedChats = new LinkedHashSet<>();

    @OneToMany(mappedBy = "sender")
    private Set<Message> messages = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<Notification> notifications = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<Order> orders = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<Review> reviews = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<Shop> shops = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<UserLogin> userLogins = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<Wishlist> wishlists = new LinkedHashSet<>();

}