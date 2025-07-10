package com.tientrannnn.models;

import jakarta.persistence.*;
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
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "shipping_fee", precision = 18, scale = 2)
    private BigDecimal shippingFee;

    @Column(name = "discount_amount", precision = 18, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "final_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal finalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Nationalized
    @Column(name = "shipping_address", nullable = false)
    private String shippingAddress;

    @Column(name = "shipping_phone", nullable = false)
    private String shippingPhone;

    @Nationalized
    @Column(name = "recipient_name", nullable = false)
    private String recipientName;

    @Nationalized
    @Column(name = "notes")
    private String notes;

    @ColumnDefault("getdate()")
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @ColumnDefault("getdate()")
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<OrderItem> orderItems = new LinkedHashSet<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Payment> payments = new LinkedHashSet<>();

    public enum OrderStatus {
        PENDING,
        CONFIRMED,
        PROCESSING,
        SHIPPED,
        DELIVERED,
        CANCELLED,
        RETURNED
    }

    public enum PaymentStatus {
        PENDING,
        PAID,
        FAILED,
        REFUNDED
    }
}