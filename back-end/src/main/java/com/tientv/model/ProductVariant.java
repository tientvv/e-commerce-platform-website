package com.tientv.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
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

    @Size(max = 100)
    @Nationalized
    @Column(name = "variant_name", length = 100)
    private String variantName;

    @Size(max = 100)
    @Nationalized
    @Column(name = "variant_value", length = 100)
    private String variantValue;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "price", precision = 18, scale = 2)
    private BigDecimal price;

    @ColumnDefault("1")
    @Column(name = "is_active")
    private Boolean isActive;

    @OneToMany(mappedBy = "productVariant")
    private Set<Discount> discounts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "productVariant")
    private Set<OrderItem> orderItems = new LinkedHashSet<>();

    @OneToMany(mappedBy = "productVariant")
    private Set<ProductImage> productImages = new LinkedHashSet<>();

    @OneToMany(mappedBy = "productVariant")
    private Set<Review> reviews = new LinkedHashSet<>();

}