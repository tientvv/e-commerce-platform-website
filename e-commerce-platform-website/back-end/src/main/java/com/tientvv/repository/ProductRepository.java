package com.tientvv.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tientvv.dto.CrudProduct.ProductDto;
import com.tientvv.dto.CrudProduct.ProductDisplayDto;
import com.tientvv.dto.CrudProduct.ProductDetailDto;
import com.tientvv.model.Product;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
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
                 (SELECT CASE
                   WHEN d2.discountType = 'PERCENTAGE' THEN d2.discountValue
                   ELSE 0
                 END
                 FROM Discount d2
                 WHERE d2.isActive = true
                   AND d2.startDate <= CURRENT_TIMESTAMP
                   AND d2.endDate >= CURRENT_TIMESTAMP
                   AND (d2.product = p OR d2.category = c OR (d2.product IS NULL AND d2.category IS NULL AND d2.productVariant IS NULL))
                 ORDER BY d2.discountValue DESC
                 LIMIT 1) AS discountPercentage,
                 (SELECT CASE
                   WHEN d2.discountType = 'FIXED' THEN d2.discountValue
                   ELSE 0
                 END
                 FROM Discount d2
                 WHERE d2.isActive = true
                   AND d2.startDate <= CURRENT_TIMESTAMP
                   AND d2.endDate >= CURRENT_TIMESTAMP
                   AND (d2.product = p OR d2.category = c OR (d2.product IS NULL AND d2.category IS NULL AND d2.productVariant IS NULL))
                 ORDER BY d2.discountValue DESC
                 LIMIT 1) AS discountAmount,
                 (SELECT d2.discountType
                 FROM Discount d2
                 WHERE d2.isActive = true
                   AND d2.startDate <= CURRENT_TIMESTAMP
                   AND d2.endDate >= CURRENT_TIMESTAMP
                   AND (d2.product = p OR d2.category = c OR (d2.product IS NULL AND d2.category IS NULL AND d2.productVariant IS NULL))
                 ORDER BY d2.discountValue DESC
                 LIMIT 1) AS discountType,
                 (SELECT d2.name
                 FROM Discount d2
                 WHERE d2.isActive = true
                   AND d2.startDate <= CURRENT_TIMESTAMP
                   AND d2.endDate >= CURRENT_TIMESTAMP
                   AND (d2.product = p OR d2.category = c OR (d2.product IS NULL AND d2.category IS NULL AND d2.productVariant IS NULL))
                 ORDER BY d2.discountValue DESC
                 LIMIT 1) AS discountName,
                 (SELECT d2.startDate
                 FROM Discount d2
                 WHERE d2.isActive = true
                   AND d2.startDate <= CURRENT_TIMESTAMP
                   AND d2.endDate >= CURRENT_TIMESTAMP
                   AND (d2.product = p OR d2.category = c OR (d2.product IS NULL AND d2.category IS NULL AND d2.productVariant IS NULL))
                 ORDER BY d2.discountValue DESC
                 LIMIT 1) AS discountStartDate,
                 (SELECT d2.endDate
                 FROM Discount d2
                 WHERE d2.isActive = true
                   AND d2.startDate <= CURRENT_TIMESTAMP
                   AND d2.endDate >= CURRENT_TIMESTAMP
                   AND (d2.product = p OR d2.category = c OR (d2.product IS NULL AND d2.category IS NULL AND d2.productVariant IS NULL))
                 ORDER BY d2.discountValue DESC
                 LIMIT 1) AS discountEndDate,
                 (SELECT d2.minOrderValue
                 FROM Discount d2
                 WHERE d2.isActive = true
                   AND d2.startDate <= CURRENT_TIMESTAMP
                   AND d2.endDate >= CURRENT_TIMESTAMP
                   AND (d2.product = p OR d2.category = c OR (d2.product IS NULL AND d2.category IS NULL AND d2.productVariant IS NULL))
                 ORDER BY d2.discountValue DESC
                 LIMIT 1) AS minOrderValue
          FROM Product p
          JOIN p.category c
          JOIN p.shop s
          LEFT JOIN p.productVariants pv ON pv.isActive = true
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
                 (SELECT CASE
                   WHEN d2.discountType = 'PERCENTAGE' THEN d2.discountValue
                   ELSE 0
                 END
                 FROM Discount d2
                 WHERE d2.isActive = true
                   AND d2.startDate <= CURRENT_TIMESTAMP
                   AND d2.endDate >= CURRENT_TIMESTAMP
                   AND (d2.product = p OR d2.category = c OR (d2.product IS NULL AND d2.category IS NULL AND d2.productVariant IS NULL))
                 ORDER BY d2.discountValue DESC
                 LIMIT 1) AS discountPercentage,
                 (SELECT CASE
                   WHEN d2.discountType = 'FIXED' THEN d2.discountValue
                   ELSE 0
                 END
                 FROM Discount d2
                 WHERE d2.isActive = true
                   AND d2.startDate <= CURRENT_TIMESTAMP
                   AND d2.endDate >= CURRENT_TIMESTAMP
                   AND (d2.product = p OR d2.category = c OR (d2.product IS NULL AND d2.category IS NULL AND d2.productVariant IS NULL))
                 ORDER BY d2.discountValue DESC
                 LIMIT 1) AS discountAmount,
                 (SELECT d2.discountType
                 FROM Discount d2
                 WHERE d2.isActive = true
                   AND d2.startDate <= CURRENT_TIMESTAMP
                   AND d2.endDate >= CURRENT_TIMESTAMP
                   AND (d2.product = p OR d2.category = c OR (d2.product IS NULL AND d2.category IS NULL AND d2.productVariant IS NULL))
                 ORDER BY d2.discountValue DESC
                 LIMIT 1) AS discountType,
                 (SELECT d2.name
                 FROM Discount d2
                 WHERE d2.isActive = true
                   AND d2.startDate <= CURRENT_TIMESTAMP
                   AND d2.endDate >= CURRENT_TIMESTAMP
                   AND (d2.product = p OR d2.category = c OR (d2.product IS NULL AND d2.category IS NULL AND d2.productVariant IS NULL))
                 ORDER BY d2.discountValue DESC
                 LIMIT 1) AS discountName,
                 (SELECT d2.startDate
                 FROM Discount d2
                 WHERE d2.isActive = true
                   AND d2.startDate <= CURRENT_TIMESTAMP
                   AND d2.endDate >= CURRENT_TIMESTAMP
                   AND (d2.product = p OR d2.category = c OR (d2.product IS NULL AND d2.category IS NULL AND d2.productVariant IS NULL))
                 ORDER BY d2.discountValue DESC
                 LIMIT 1) AS discountStartDate,
                 (SELECT d2.endDate
                 FROM Discount d2
                 WHERE d2.isActive = true
                   AND d2.startDate <= CURRENT_TIMESTAMP
                   AND d2.endDate >= CURRENT_TIMESTAMP
                   AND (d2.product = p OR d2.category = c OR (d2.product IS NULL AND d2.category IS NULL AND d2.productVariant IS NULL))
                 ORDER BY d2.discountValue DESC
                 LIMIT 1) AS discountEndDate,
                 (SELECT d2.minOrderValue
                 FROM Discount d2
                 WHERE d2.isActive = true
                   AND d2.startDate <= CURRENT_TIMESTAMP
                   AND d2.endDate >= CURRENT_TIMESTAMP
                   AND (d2.product = p OR d2.category = c OR (d2.product IS NULL AND d2.category IS NULL AND d2.productVariant IS NULL))
                 ORDER BY d2.discountValue DESC
                 LIMIT 1) AS minOrderValue,
                 COALESCE(AVG(r.rating), 0) AS rating,
                 COUNT(r.id) AS reviewCount
          FROM Product p
          JOIN p.category c
          JOIN p.shop s
          LEFT JOIN p.productVariants pv ON pv.isActive = true
          LEFT JOIN p.reviews r
          WHERE p.id = :productId
          GROUP BY p.id, p.name, p.brand, p.description, p.productImage, p.isActive,
                   p.viewCount, p.soldCount, c.name, c.id, s.shopName, s.id
      """)
  ProductDetailDto findProductDetailById(@Param("productId") UUID productId);

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
                 CASE
                   WHEN d.discountType = 'PERCENTAGE' THEN d.discountValue
                   ELSE 0
                 END AS discountPercentage,
                 CASE
                   WHEN d.discountType = 'FIXED' THEN d.discountValue
                   ELSE 0
                 END AS discountAmount,
                 d.discountType AS discountType,
                 d.name AS discountName,
                 d.startDate AS discountStartDate,
                 d.endDate AS discountEndDate,
                 d.minOrderValue AS minOrderValue
          FROM Product p
          JOIN p.category c
          JOIN p.shop s
          LEFT JOIN p.productVariants pv ON pv.isActive = true
          INNER JOIN Discount d ON d.isActive = true
              AND d.startDate <= CURRENT_TIMESTAMP
              AND d.endDate >= CURRENT_TIMESTAMP
              AND (
                  d.product = p
                  OR d.category = c
                  OR (d.product IS NULL AND d.category IS NULL AND d.productVariant IS NULL)
              )
          WHERE p.isActive = true
          GROUP BY p.id, p.name, p.brand, p.description, p.productImage, p.isActive,
                   p.viewCount, p.soldCount, c.name, c.id, s.shopName,
                   d.discountType, d.discountValue, d.name, d.startDate, d.endDate, d.minOrderValue
          ORDER BY d.discountValue DESC, p.viewCount DESC
      """)
  List<ProductDisplayDto> findActiveProductsWithActiveDiscounts();

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
                 CASE
                   WHEN d.discountType = 'PERCENTAGE' THEN d.discountValue
                   ELSE 0
                 END AS discountPercentage,
                 CASE
                   WHEN d.discountType = 'FIXED' THEN d.discountValue
                   ELSE 0
                 END AS discountAmount,
                 d.discountType AS discountType,
                 d.name AS discountName,
                 d.startDate AS discountStartDate,
                 d.endDate AS discountEndDate,
                 d.minOrderValue AS minOrderValue
          FROM Product p
          JOIN p.category c
          JOIN p.shop s
          LEFT JOIN p.productVariants pv ON pv.isActive = true
          INNER JOIN Discount d ON d.isActive = true
              AND d.startDate <= CURRENT_TIMESTAMP
              AND d.endDate >= CURRENT_TIMESTAMP
              AND d.product = p
          WHERE p.isActive = true
          GROUP BY p.id, p.name, p.brand, p.description, p.productImage, p.isActive,
                   p.viewCount, p.soldCount, c.name, c.id, s.shopName,
                   d.discountType, d.discountValue, d.name, d.startDate, d.endDate, d.minOrderValue
          ORDER BY d.discountValue DESC, p.viewCount DESC
      """)
  List<ProductDisplayDto> findActiveProductsWithProductSpecificDiscounts();

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
                 0 AS minPrice,
                 0 AS maxPrice,
                 0 AS originalPrice,
                 d.discountValue AS discountPercentage,
                 0 AS discountAmount,
                 d.discountType AS discountType,
                 d.name AS discountName,
                 d.startDate AS discountStartDate,
                 d.endDate AS discountEndDate,
                 d.minOrderValue AS minOrderValue
          FROM Product p
          JOIN p.category c
          JOIN p.shop s
          INNER JOIN Discount d ON d.isActive = true
              AND d.startDate <= CURRENT_TIMESTAMP
              AND d.endDate >= CURRENT_TIMESTAMP
              AND d.product = p
          WHERE p.isActive = true
          ORDER BY d.discountValue DESC, p.viewCount DESC
      """)
  List<ProductDisplayDto> findActiveProductsWithDiscountsSimple();

  // Không cần method này nữa vì sẽ sử dụng Specification
}