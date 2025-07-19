package com.tientv.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @Size(max = 255)
    @Column(name = "transaction_code")
    private String transactionCode;

    @Column(name = "transaction_amount", precision = 18, scale = 2)
    private BigDecimal transactionAmount;

    @Size(max = 50)
    @Column(name = "transaction_status", length = 50)
    private String transactionStatus;

    @Column(name = "transaction_date")
    private OffsetDateTime transactionDate;

    @Size(max = 50)
    @Column(name = "vnpay_response_code", length = 50)
    private String vnpayResponseCode;

    @OneToMany(mappedBy = "transaction")
    private Set<ReturnRefund> returnRefunds = new LinkedHashSet<>();

}