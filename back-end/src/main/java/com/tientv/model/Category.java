package com.tientv.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 255)
    @Nationalized
    @Column(name = "name")
    private String name;

    @Lob
    @Column(name = "category_image")
    private String categoryImage;

    @OneToMany(mappedBy = "category")
    private Set<Discount> discounts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "category")
    private Set<Product> products = new LinkedHashSet<>();

}