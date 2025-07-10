package com.tientrannnn.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "brands")
public class Brand {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Nationalized
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Nationalized
    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "website_url", length = 500)
    private String websiteUrl;

    @ColumnDefault("0")
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @ColumnDefault("getdate()")
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @ColumnDefault("getdate()")
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "brand")
    private Set<Product> products = new LinkedHashSet<>();
}