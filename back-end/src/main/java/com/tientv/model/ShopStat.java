package com.tientv.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "shop_stats")
public class ShopStat {
    @Id
    @Column(name = "shop_id", nullable = false)
    private UUID id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shops;

    @Column(name = "total_sales", precision = 18, scale = 2)
    private BigDecimal totalSales;

    @Column(name = "total_orders")
    private Integer totalOrders;

    @Column(name = "last_updated")
    private OffsetDateTime lastUpdated;

}