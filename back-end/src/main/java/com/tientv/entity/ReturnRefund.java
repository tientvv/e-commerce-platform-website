package com.tientv.entity;

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
@Table(name = "return_refunds")
public class ReturnRefund {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "order_id")
    private UUID orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @Nationalized
    @Lob
    @Column(name = "return_reason")
    private String returnReason;

    @Column(name = "refund_amount", precision = 18, scale = 2)
    private BigDecimal refundAmount;

    @Size(max = 50)
    @Column(name = "return_status", length = 50)
    private String returnStatus;

    @Column(name = "request_date")
    private OffsetDateTime requestDate;

    @Column(name = "approved_date")
    private OffsetDateTime approvedDate;

    @Column(name = "completed_date")
    private OffsetDateTime completedDate;

    @Column(name = "rejected_date")
    private OffsetDateTime rejectedDate;

    @ColumnDefault("1")
    @Column(name = "is_active")
    private Boolean isActive;

}