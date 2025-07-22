package com.tientvv.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.util.LinkedHashSet;
import java.util.Set;
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

    @Size(max = 50)
    @Column(name = "payment_code", length = 50)
    private String paymentCode;

    @Size(max = 20)
    @Column(name = "payment_type", length = 20)
    private String paymentType;

    @Size(max = 100)
    @Nationalized
    @Column(name = "payment_name", length = 100)
    private String paymentName;

    @Lob
    @Column(name = "icon")
    private String icon;

    @Nationalized
    @Lob
    @Column(name = "description")
    private String description;

    @ColumnDefault("1")
    @Column(name = "is_active")
    private Boolean isActive;

    @OneToMany(mappedBy = "payment")
    private Set<Order> orders = new LinkedHashSet<>();

    @OneToMany(mappedBy = "payment")
    private Set<Transaction> transactions = new LinkedHashSet<>();

}