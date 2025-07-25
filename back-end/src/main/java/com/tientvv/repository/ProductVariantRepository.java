package com.tientvv.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tientvv.model.ProductVariant;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {
  // Additional query methods can be defined here if needed

}
