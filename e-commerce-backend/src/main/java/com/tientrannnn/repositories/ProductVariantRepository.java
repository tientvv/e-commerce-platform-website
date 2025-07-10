package com.tientrannnn.repositories;

import com.tientrannnn.models.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {
    
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.product.id = :productId AND pv.isActive = true")
    List<ProductVariant> findByProductIdActive(@Param("productId") UUID productId);
    
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.isActive = true AND pv.id = :id")
    ProductVariant findByIdActive(@Param("id") UUID id);
    
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.product.id = :productId AND pv.isDefault = true AND pv.isActive = true")
    ProductVariant findDefaultVariantByProductId(@Param("productId") UUID productId);
    
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.sku = :sku AND pv.isActive = true")
    ProductVariant findBySkuActive(@Param("sku") String sku);
}