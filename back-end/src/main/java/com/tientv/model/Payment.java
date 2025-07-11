package com.tientv.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "amount", precision = 18, scale = 2)
    private BigDecimal amount;

    @Size(max = 50)
    @Nationalized
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Size(max = 50)
    @Nationalized
    @Column(name = "status", length = 50)
    private String status;

    @Size(max = 255)
    @Nationalized
    @Column(name = "transaction_code")
    private String transactionCode;

    @Column(name = "payment_date")
    private OffsetDateTime paymentDate;

    @Size(max = 50)
    @Nationalized
    @Column(name = "payment_gateway", length = 50)
    private String paymentGateway;

    @Nationalized
    @Lob
    @Column(name = "payment_details")
    private String paymentDetails;

}