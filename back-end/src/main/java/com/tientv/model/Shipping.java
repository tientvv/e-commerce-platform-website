package com.tientv.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "shipping")
public class Shipping {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Size(max = 100)
    @Nationalized
    @Column(name = "shipping_method", length = 100)
    private String shippingMethod;

    @Size(max = 100)
    @Nationalized
    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Size(max = 50)
    @Nationalized
    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "estimated_delivery")
    private OffsetDateTime estimatedDelivery;

    @Column(name = "actual_delivery")
    private OffsetDateTime actualDelivery;

    @Nationalized
    @Lob
    @Column(name = "shipping_address")
    private String shippingAddress;

}