package com.tientrannnn.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @ColumnDefault("'USER'")
    @Column(name = "role", nullable = false, length = 50)
    private String role;

    @ColumnDefault("0")
    @Column(name = "is_seller", nullable = false)
    private Boolean isSeller = false;

    @ColumnDefault("0")
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Nationalized
    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "gender")
    private Boolean gender;

    @ColumnDefault("getdate()")
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @ColumnDefault("getdate()")
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "phone", length = 13)
    private String phone;

    @Nationalized
    @Column(name = "address", length = 500)
    private String address;

    @OneToOne(mappedBy = "user")
    private Shop shop;
}