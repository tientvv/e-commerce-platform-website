package com.tientv.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Size(max = 255)
    @Nationalized
    @Column(name = "slug")
    private String slug;

    @Nationalized
    @Lob
    @Column(name = "short_description")
    private String shortDescription;

    @Size(max = 500)
    @Nationalized
    @Column(name = "search_keywords", length = 500)
    private String searchKeywords;

    @ColumnDefault("0")
    @Column(name = "view_count")
    private Integer viewCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @ColumnDefault("0")
    @Column(name = "is_featured")
    private Boolean isFeatured;

    @OneToMany(mappedBy = "product")
    private Set<Chat> chats = new LinkedHashSet<>();

    @OneToMany(mappedBy = "product")
    private Set<CouponProduct> couponProducts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "product")
    private Set<ProductVariant> productVariants = new LinkedHashSet<>();

    @OneToMany(mappedBy = "product")
    private Set<PromotionProduct> promotionProducts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "product")
    private Set<Review> reviews = new LinkedHashSet<>();

    @OneToMany(mappedBy = "product")
    private Set<Wishlist> wishlists = new LinkedHashSet<>();

}