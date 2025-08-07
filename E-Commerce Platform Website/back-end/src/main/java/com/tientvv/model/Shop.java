package com.tientvv.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "shops")
public class Shop {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Lob
    @Column(name = "shop_image")
    private String shopImage;

    @Nationalized
    @Lob
    @Column(name = "shop_name")
    private String shopName;

    @Nationalized
    @Lob
    @Column(name = "description")
    private String description;

    @Size(max = 255)
    @Nationalized
    @Column(name = "phone")
    private String phone;

    @Size(max = 255)
    @Nationalized
    @Column(name = "email")
    private String email;

    @Nationalized
    @Lob
    @Column(name = "address")
    private String address;

    @Column(name = "rating", precision = 18, scale = 2)
    private BigDecimal rating;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "last_updated")
    private OffsetDateTime lastUpdated;

    @ColumnDefault("1")
    @Column(name = "is_active")
    private Boolean isActive;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Account user;

    @JsonIgnore
    @OneToMany(mappedBy = "shop")
    private Set<Order> orders = new LinkedHashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "shop")
    private Set<Product> products = new LinkedHashSet<>();

}