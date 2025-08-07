package com.tientvv.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "product_price", precision = 18, scale = 2)
    private BigDecimal productPrice;

    @ColumnDefault("0")
    @Column(name = "discount_applied", precision = 18, scale = 2)
    private BigDecimal discountApplied;

    @OneToMany(mappedBy = "orderItem")
    private Set<ReturnRefund> returnRefunds = new LinkedHashSet<>();

}