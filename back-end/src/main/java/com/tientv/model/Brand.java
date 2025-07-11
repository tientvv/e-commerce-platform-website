package com.tientv.model;

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
@Table(name = "brands")
public class Brand {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 255)
    @Nationalized
    @Column(name = "name")
    private String name;

    @Lob
    @Column(name = "logo_url")
    private String logoUrl;

    @ColumnDefault("0")
    @Column(name = "is_popular")
    private Boolean isPopular;

    @OneToMany(mappedBy = "brand")
    private Set<Product> products = new LinkedHashSet<>();

}