package com.tientv.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "product_variants")
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Size(max = 500)
    @Nationalized
    @Column(name = "name", length = 500)
    private String name;

    @Column(name = "price", precision = 18, scale = 2)
    private BigDecimal price;

    @Column(name = "original_price", precision = 18, scale = 2)
    private BigDecimal originalPrice;

    @Size(max = 50)
    @Nationalized
    @Column(name = "\"size\"", length = 50)
    private String size;

    @Size(max = 50)
    @Nationalized
    @Column(name = "color", length = 50)
    private String color;

    @Column(name = "quantity")
    private Integer quantity;

    @Lob
    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "productVariant")
    private Set<CouponProduct> couponProducts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "productVariant")
    private Set<FlashSaleItem> flashSaleItems = new LinkedHashSet<>();

    @OneToMany(mappedBy = "productVariant")
    private Set<Inventory> inventories = new LinkedHashSet<>();

    @OneToMany(mappedBy = "productVariant")
    private Set<InventoryLog> inventoryLogs = new LinkedHashSet<>();

    @OneToMany(mappedBy = "productVariant")
    private Set<OrderItem> orderItems = new LinkedHashSet<>();

    @OneToMany(mappedBy = "productVariant")
    private Set<ProductImage> productImages = new LinkedHashSet<>();

    @OneToMany(mappedBy = "productVariant")
    private Set<PromotionProduct> promotionProducts = new LinkedHashSet<>();

}