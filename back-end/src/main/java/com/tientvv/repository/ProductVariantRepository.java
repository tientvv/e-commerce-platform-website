package com.tientvv.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tientvv.model.ProductVariant;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

  @Query("SELECT pv FROM ProductVariant pv WHERE pv.product.id = :productId AND pv.isActive = true")
  List<ProductVariant> findByProductIdAndIsActiveTrue(@Param("productId") UUID productId);

  @Query("SELECT pv FROM ProductVariant pv WHERE pv.product.id = :productId")
  List<ProductVariant> findByProductId(@Param("productId") UUID productId);

  boolean existsByProductIdAndVariantNameAndVariantValue(UUID productId, String variantName, String variantValue);

  List<ProductVariant> findByIsActiveTrue();
}
