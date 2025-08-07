package com.tientvv.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tientvv.model.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {

  @Query("SELECT pi FROM ProductImage pi WHERE pi.product.id = :productId")
  List<ProductImage> findByProductId(@Param("productId") UUID productId);

  @Query("SELECT pi FROM ProductImage pi WHERE pi.productVariant.id = :productVariantId")
  List<ProductImage> findByProductVariantId(@Param("productVariantId") UUID productVariantId);

  @Query("SELECT pi FROM ProductImage pi WHERE pi.product.id = :productId AND pi.productVariant IS NULL")
  List<ProductImage> findByProductIdAndProductVariantIsNull(@Param("productId") UUID productId);

  @Query("SELECT pi FROM ProductImage pi WHERE pi.product.id = :productId AND pi.productVariant IS NOT NULL")
  List<ProductImage> findByProductIdAndProductVariantIsNotNull(@Param("productId") UUID productId);

  @Query("SELECT pi FROM ProductImage pi WHERE pi.product.shop.id = :shopId")
  List<ProductImage> findByProductShopId(@Param("shopId") UUID shopId);
}