package com.tientrannnn.models;

import jakarta.persistence.*;
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
@Table(name = "shops")
public class Shop {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Nationalized
    @Column(name = "name_shop", nullable = false)
    private String nameShop;

    @Nationalized
    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "avatar_url", nullable = false, length = 500)
    private String avatarUrl;

    @ColumnDefault("getdate()")
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @ColumnDefault("getdate()")
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @ColumnDefault("0")
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "cccd", nullable = false, length = 50)
    private String cccd;

    @Column(name = "cccd_front_url", nullable = false, length = 500)
    private String cccdFrontUrl;

    @Column(name = "cccd_back_url", nullable = false, length = 500)
    private String cccdBackUrl;

    @ColumnDefault("0")
    @Column(name = "status", nullable = false)
    private Boolean status = false;

    @Nationalized
    @Lob
    @Column(name = "reason")
    private String reason;

    @ColumnDefault("0")
    @Column(name = "is_seller", nullable = false)
    private Boolean isSeller = false;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "shop")
    private Set<Product> products = new LinkedHashSet<>();

}