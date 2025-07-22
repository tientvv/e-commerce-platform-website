package com.tientvv.model;

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
    @JoinColumn(name = "category_id")
    private Category category;

    @Size(max = 50)
    @Nationalized
    @Column(name = "brand", length = 50)
    private String brand;

    @Size(max = 255)
    @Nationalized
    @Column(name = "name")
    private String name;

    @Lob
    @Column(name = "product_image")
    private String productImage;

    @Nationalized
    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "view_count")
    private Integer viewCount;

    @Column(name = "sold_count")
    private Integer soldCount;

    @ColumnDefault("1")
    @Column(name = "is_active")
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @OneToMany(mappedBy = "product")
    private Set<Discount> discounts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "product")
    private Set<ProductImage> productImages = new LinkedHashSet<>();

    @OneToMany(mappedBy = "product")
    private Set<ProductVariant> productVariants = new LinkedHashSet<>();

    @OneToMany(mappedBy = "product")
    private Set<Review> reviews = new LinkedHashSet<>();

    @OneToMany(mappedBy = "product")
    private Set<Wishlist> wishlists = new LinkedHashSet<>();

}