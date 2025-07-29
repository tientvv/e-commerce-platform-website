package com.tientvv.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tientvv.dto.CrudProduct.ProductDto;
import com.tientvv.dto.CrudProduct.ProductDisplayDto;
import com.tientvv.dto.CrudProduct.ProductDetailDto;
import com.tientvv.model.Product;

public interface ProductRepository extends JpaRepository<Product, UUID> {
  @Query("""
          SELECT p.id AS id,
                 p.name AS name,
                 p.brand AS brand,
                 p.description AS description,
                 p.productImage AS productImage,
                 p.isActive AS isActive,
                 c.name AS categoryName
          FROM Product p
          JOIN p.category c
          WHERE p.shop.id = :shopId AND p.isActive = :isActive
      """)
  List<ProductDto> findByShopIdAndIsActive(@Param("shopId") UUID shopId, @Param("isActive") Boolean isActive);

  @Query("SELECT p FROM Product p WHERE p.shop.id = :shopId")
  List<Product> findAllByShopId(@Param("shopId") UUID shopId);

  @Query("SELECT p FROM Product p WHERE p.shop.id = :shopId AND p.isActive = true")
  List<Product> findAllByShopIdAndIsActiveTrue(@Param("shopId") UUID shopId);

  @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.isActive = true")
  List<Product> findAllByCategoryIdAndIsActiveTrue(@Param("categoryId") UUID categoryId);

  List<Product> findByIsActiveTrue();

  @Query("""
          SELECT p.id AS id,
                 p.name AS name,
                 p.brand AS brand,
                 p.description AS description,
                 p.productImage AS productImage,
                 p.isActive AS isActive,
                 p.viewCount AS viewCount,
                 p.soldCount AS soldCount,
                 c.name AS categoryName,
                 c.id AS categoryId,
                 s.shopName AS shopName,
                 COALESCE(MIN(pv.price), 0) AS minPrice,
                 COALESCE(MAX(pv.price), 0) AS maxPrice,
                 COALESCE(MIN(pv.price), 0) AS originalPrice,
                 COALESCE(MAX(d.discountValue), 0) AS discountPercentage
          FROM Product p
          JOIN p.category c
          JOIN p.shop s
          LEFT JOIN p.productVariants pv ON pv.isActive = true
          LEFT JOIN p.discounts d ON d.isActive = true
              AND d.startDate <= CURRENT_TIMESTAMP
              AND d.endDate >= CURRENT_TIMESTAMP
          WHERE p.isActive = true
          GROUP BY p.id, p.name, p.brand, p.description, p.productImage, p.isActive,
                   p.viewCount, p.soldCount, c.name, c.id, s.shopName
          ORDER BY p.viewCount DESC
      """)
  List<ProductDisplayDto> findActiveProductsWithPricing();

  @Query("""
          SELECT p.id AS id,
                 p.name AS name,
                 p.brand AS brand,
                 p.description AS description,
                 p.productImage AS productImage,
                 p.isActive AS isActive,
                 p.viewCount AS viewCount,
                 p.soldCount AS soldCount,
                 c.name AS categoryName,
                 c.id AS categoryId,
                 s.shopName AS shopName,
                 COALESCE(MIN(pv.price), 0) AS minPrice,
                 COALESCE(MAX(pv.price), 0) AS maxPrice,
                 COALESCE(MIN(pv.price), 0) AS originalPrice,
                 COALESCE(MAX(d.discountValue), 0) AS discountPercentage
          FROM Product p
          JOIN p.category c
          JOIN p.shop s
          LEFT JOIN p.productVariants pv ON pv.isActive = true
          LEFT JOIN p.discounts d ON d.isActive = true
              AND d.startDate <= CURRENT_TIMESTAMP
              AND d.endDate >= CURRENT_TIMESTAMP
          WHERE p.isActive = true AND c.id = :categoryId
          GROUP BY p.id, p.name, p.brand, p.description, p.productImage, p.isActive,
                   p.viewCount, p.soldCount, c.name, c.id, s.shopName
          ORDER BY p.viewCount DESC
      """)
  List<ProductDisplayDto> findActiveProductsWithPricingByCategoryId(@Param("categoryId") UUID categoryId);

  @Query("""
          SELECT p.id AS id,
                 p.name AS name,
                 p.brand AS brand,
                 p.description AS description,
                 p.productImage AS productImage,
                 p.isActive AS isActive,
                 p.viewCount AS viewCount,
                 p.soldCount AS soldCount,
                 c.name AS categoryName,
                 c.id AS categoryId,
                 s.shopName AS shopName,
                 s.id AS shopId,
                 COALESCE(MIN(pv.price), 0) AS minPrice,
                 COALESCE(MAX(pv.price), 0) AS maxPrice,
                 COALESCE(MIN(pv.price), 0) AS originalPrice,
                 COALESCE(MAX(d.discountValue), 0) AS discountPercentage,
                 COALESCE(AVG(r.rating), 0) AS rating,
                 COUNT(r.id) AS reviewCount
          FROM Product p
          JOIN p.category c
          JOIN p.shop s
          LEFT JOIN p.productVariants pv ON pv.isActive = true
          LEFT JOIN p.discounts d ON d.isActive = true
              AND d.startDate <= CURRENT_TIMESTAMP
              AND d.endDate >= CURRENT_TIMESTAMP
          LEFT JOIN p.reviews r
          WHERE p.id = :productId
          GROUP BY p.id, p.name, p.brand, p.description, p.productImage, p.isActive,
                   p.viewCount, p.soldCount, c.name, c.id, s.shopName, s.id
      """)
  ProductDetailDto findProductDetailById(@Param("productId") UUID productId);
}
