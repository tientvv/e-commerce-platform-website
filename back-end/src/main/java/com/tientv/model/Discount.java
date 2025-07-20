package com.tientv.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "discounts")
public class Discount {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 255)
    @Nationalized
    @Column(name = "name")
    private String name;

    @Nationalized
    @Lob
    @Column(name = "description")
    private String description;

    @Size(max = 50)
    @Column(name = "discount_type", length = 50)
    private String discountType;

    @Column(name = "discount_value", precision = 18, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "start_date")
    private OffsetDateTime startDate;

    @Column(name = "end_date")
    private OffsetDateTime endDate;

    @Column(name = "min_order_value", precision = 18, scale = 2)
    private BigDecimal minOrderValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

    @ColumnDefault("1")
    @Column(name = "is_active")
    private Boolean isActive;

}