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

import java.time.OffsetDateTime;

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
              AND d.startDate <= :currentTime
              AND d.endDate >= :currentTime
          WHERE p.isActive = true
          GROUP BY p.id, p.name, p.brand, p.description, p.productImage, p.isActive,
                   p.viewCount, p.soldCount, c.name, c.id, s.shopName
          ORDER BY p.viewCount DESC
      """)
  List<ProductDisplayDto> findActiveProductsWithPricing(@Param("currentTime") OffsetDateTime currentTime);

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
                   AND d2.startDate <= :currentTime
                   AND d2.endDate >= :currentTime
                   AND (d2.product = p OR d2.category = c OR (d2.product IS NULL AND d2.category IS NULL AND d2.productVariant IS NULL))
                   AND (d2.minOrderValue IS NULL OR d2.minOrderValue <= COALESCE(MIN(pv.price), 0))
                 ORDER BY 
                   CASE 
                     WHEN d2.discountType = 'PERCENTAGE' THEN 1
                     WHEN d2.discountType = 'FIXED' THEN 2
                     ELSE 3
                   END,
                   d2.discountValue DESC
                 LIMIT 1) AS discountPercentage,
                 (SELECT CASE
                   WHEN d2.discountType = 'FIXED' THEN d2.discountValue
                   ELSE 0
                 END
                 FROM Discount d2
                 WHERE d2.isActive = true
                   AND d2.startDate <= :currentTime
                   AND d2.endDate >= :currentTime
                   AND (d2.product = p OR d2.category = c OR (d2.product IS NULL AND d2.category IS NULL AND d2.productVariant IS NULL))
                   AND (d2.minOrderValue IS NULL OR d2.minOrderValue <= COALESCE(MIN(pv.price), 0))
                 ORDER BY 
                   CASE 
                     WHEN d2.discountType = 'PERCENTAGE' THEN 1
                     WHEN d2.discountType = 'FIXED' THEN 2
                     ELSE 3
                   END,
                   d2.discountValue DESC
                 LIMIT 1) AS discountAmount,
                 (SELECT d2.discountType
                 FROM Discount d2
                 WHERE d2.isActive = true
                   AND d2.startDate <= :currentTime
                   AND d2.endDate >= :currentTime
                   AND (d2.product = p OR d2.category = c OR (d2.product IS NULL AND d2.category IS NULL AND d2.productVariant IS NULL))
                   AND (d2.minOrderValue IS NULL OR d2.minOrderValue <= COALESCE(MIN(pv.price), 0))
                 ORDER BY 
                   CASE 
                     WHEN d2.discountType = 'PERCENTAGE' THEN 1
                     WHEN d2.discountType = 'FIXED' THEN 2
                     ELSE 3
                   END,
                   d2.discountValue DESC
                 LIMIT 1) AS discountType,
                 (SELECT d2.name
                 FROM Discount d2
                 WHERE d2.isActive = true
                   AND d2.startDate <= :currentTime
                   AND d2.endDate >= :currentTime
                   AND (d2.product = p OR d2.category = c OR (d2.product IS NULL AND d2.category IS NULL AND d2.productVariant IS NULL))
                   AND (d2.minOrderValue IS NULL OR d2.minOrderValue <= COALESCE(MIN(pv.price), 0))
                 ORDER BY 
                   CASE 
                     WHEN d2.discountType = 'PERCENTAGE' THEN 1
                     WHEN d2.discountType = 'FIXED' THEN 2
                     ELSE 3
                   END,
                   d2.discountValue DESC
                 LIMIT 1) AS discountName,
                 (SELECT d2.startDate
                 FROM Discount d2
                 WHERE d2.isActive = true
                   AND d2.startDate <= :currentTime
                   AND d2.endDate >= :currentTime
                   AND (d2.product = p OR d2.category = c OR (d2.product IS NULL AND d2.category IS NULL AND d2.productVariant IS NULL))
                   AND (d2.minOrderValue IS NULL OR d2.minOrderValue <= COALESCE(MIN(pv.price), 0))
                 ORDER BY 
                   CASE 
                     WHEN d2.discountType = 'PERCENTAGE' THEN 1
                     WHEN d2.discountType = 'FIXED' THEN 2
                     ELSE 3
                   END,
                   d2.discountValue DESC
                 LIMIT 1) AS discountStartDate,
                                   (SELECT d2.endDate
                  FROM Discount d2
                  WHERE d2.isActive = true
                    AND d2.startDate <= :currentTime
                    AND d2.endDate >= :currentTime
                    AND (d2.product = p OR d2.category = c OR (d2.product IS NULL AND d2.category IS NULL AND d2.productVariant IS NULL))
                    AND (d2.minOrderValue IS NULL OR d2.minOrderValue <= COALESCE(MIN(pv.price), 0))
                  ORDER BY 
                    CASE 
                      WHEN d2.discountType = 'PERCENTAGE' THEN 1
                      WHEN d2.discountType = 'FIXED' THEN 2
                      ELSE 3
                    END,
                    d2.discountValue DESC
                  LIMIT 1) AS discountEndDate,
                                   (SELECT d2.minOrderValue
                  FROM Discount d2
                  WHERE d2.isActive = true
                    AND d2.startDate <= :currentTime
                    AND d2.endDate >= :currentTime
                    AND (d2.product = p OR d2.category = c OR (d2.product IS NULL AND d2.category IS NULL AND d2.productVariant IS NULL))
                    AND (d2.minOrderValue IS NULL OR d2.minOrderValue <= COALESCE(MIN(pv.price), 0))
                  ORDER BY 
                    CASE 
                      WHEN d2.discountType = 'PERCENTAGE' THEN 1
                      WHEN d2.discountType = 'FIXED' THEN 2
                      ELSE 3
                    END,
                    d2.discountValue DESC
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
  List<ProductDisplayDto> findActiveProductsWithPricingByCategoryId(@Param("categoryId") UUID categoryId, @Param("currentTime") OffsetDateTime currentTime);

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
                 s.isActive AS shopIsActive,
                 COALESCE(MIN(pv.price), 0) AS minPrice,
                 COALESCE(MAX(pv.price), 0) AS maxPrice,
                 COALESCE(MIN(pv.price), 0) AS originalPrice,
                                   (SELECT CASE
                    WHEN d2.discountType = 'PERCENTAGE' THEN d2.discountValue
                    ELSE 0
                  END
                  FROM Discount d2
                  WHERE d2.isActive = true
                    AND d2.startDate <= :currentTime
                    AND d2.endDate >= :currentTime
                    AND (d2.product = p OR d2.category = c OR (d2.product IS NULL AND d2.category IS NULL AND d2.productVariant IS NULL))
                    AND (d2.minOrderValue IS NULL OR d2.minOrderValue <= COALESCE(MIN(pv.price), 0))
                  ORDER BY 
                    CASE 
                      WHEN d2.discountType = 'PERCENTAGE' THEN 1
                      WHEN d2.discountType = 'FIXED' THEN 2
                      ELSE 3
                    END,
                    d2.discountValue DESC
                  LIMIT 1) AS discountPercentage,
                                   (SELECT CASE
                    WHEN d2.discountType = 'FIXED' THEN d2.discountValue
                    ELSE 0
                  END
                  FROM Discount d2
                  WHERE d2.isActive = true
                    AND d2.startDate <= :currentTime
                    AND d2.endDate >= :currentTime
                    AND (d2.product = p OR d2.category = c OR (d2.product IS NULL AND d2.category IS NULL AND d2.productVariant IS NULL))
                    AND (d2.minOrderValue IS NULL OR d2.minOrderValue <= COALESCE(MIN(pv.price), 0))
                  ORDER BY 
                    CASE 
                      WHEN d2.discountType = 'PERCENTAGE' THEN 1
                      WHEN d2.discountType = 'FIXED' THEN 2
                      ELSE 3
                    END,
                    d2.discountValue DESC
                  LIMIT 1) AS discountAmount,
                                   (SELECT d2.discountType
                  FROM Discount d2
                  WHERE d2.isActive = true
                    AND d2.startDate <= :currentTime
                    AND d2.endDate >= :currentTime
                    AND (d2.product = p OR d2.category = c OR (d2.product IS NULL AND d2.category IS NULL AND d2.productVariant IS NULL))
                    AND (d2.minOrderValue IS NULL OR d2.minOrderValue <= COALESCE(MIN(pv.price), 0))
                  ORDER BY 
                    CASE 
                      WHEN d2.discountType = 'PERCENTAGE' THEN 1
                      WHEN d2.discountType = 'FIXED' THEN 2
                      ELSE 3
                    END,
                    d2.discountValue DESC
                  LIMIT 1) AS discountType,
                                   (SELECT d2.name
                  FROM Discount d2
                  WHERE d2.isActive = true
                    AND d2.startDate <= :currentTime
                    AND d2.endDate >= :currentTime
                    AND (d2.product = p OR d2.category = c OR (d2.product IS NULL AND d2.category IS NULL AND d2.productVariant IS NULL))
                    AND (d2.minOrderValue IS NULL OR d2.minOrderValue <= COALESCE(MIN(pv.price), 0))
                  ORDER BY 
                    CASE 
                      WHEN d2.discountType = 'PERCENTAGE' THEN 1
                      WHEN d2.discountType = 'FIXED' THEN 2
                      ELSE 3
                    END,
                    d2.discountValue DESC
                  LIMIT 1) AS discountName,
                                   (SELECT d2.startDate
                  FROM Discount d2
                  WHERE d2.isActive = true
                    AND d2.startDate <= :currentTime
                    AND d2.endDate >= :currentTime
                    AND (d2.product = p OR d2.category = c OR (d2.product IS NULL AND d2.category IS NULL AND d2.productVariant IS NULL))
                    AND (d2.minOrderValue IS NULL OR d2.minOrderValue <= COALESCE(MIN(pv.price), 0))
                  ORDER BY 
                    CASE 
                      WHEN d2.discountType = 'PERCENTAGE' THEN 1
                      WHEN d2.discountType = 'FIXED' THEN 2
                      ELSE 3
                    END,
                    d2.discountValue DESC
                  LIMIT 1) AS discountStartDate,
                                   (SELECT d2.endDate
                  FROM Discount d2
                  WHERE d2.isActive = true
                    AND d2.startDate <= :currentTime
                    AND d2.endDate >= :currentTime
                    AND (d2.product = p OR d2.category = c OR (d2.product IS NULL AND d2.category IS NULL AND d2.productVariant IS NULL))
                    AND (d2.minOrderValue IS NULL OR d2.minOrderValue <= COALESCE(MIN(pv.price), 0))
                  ORDER BY 
                    CASE 
                      WHEN d2.discountType = 'PERCENTAGE' THEN 1
                      WHEN d2.discountType = 'FIXED' THEN 2
                      ELSE 3
                    END,
                    d2.discountValue DESC
                  LIMIT 1) AS discountEndDate,
                  (SELECT d2.minOrderValue
                  FROM Discount d2
                  WHERE d2.isActive = true
                    AND d2.startDate <= :currentTime
                    AND d2.endDate >= :currentTime
                    AND (d2.product = p OR d2.category = c OR (d2.product IS NULL AND d2.category IS NULL AND d2.productVariant IS NULL))
                    AND (d2.minOrderValue IS NULL OR d2.minOrderValue <= COALESCE(MIN(pv.price), 0))
                  ORDER BY 
                    CASE 
                      WHEN d2.discountType = 'PERCENTAGE' THEN 1
                      WHEN d2.discountType = 'FIXED' THEN 2
                      ELSE 3
                    END,
                    d2.discountValue DESC
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
                   p.viewCount, p.soldCount, c.name, c.id, s.shopName, s.id, s.isActive
      """)
  ProductDetailDto findProductDetailById(@Param("productId") UUID productId, @Param("currentTime") OffsetDateTime currentTime);

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
                    AND d2.startDate <= :currentTime
                    AND d2.endDate >= :currentTime
                    AND (d2.product = p OR d2.category = c OR (d2.product IS NULL AND d2.category IS NULL AND d2.productVariant IS NULL))
                    AND (d2.minOrderValue IS NULL OR d2.minOrderValue <= COALESCE(MIN(pv.price), 0))
                  ORDER BY d2.discountValue DESC
                  LIMIT 1) AS discountPercentage,
                                   (SELECT CASE
                    WHEN d2.discountType = 'FIXED' THEN d2.discountValue
                    ELSE 0
                  END
                  FROM Discount d2
                  WHERE d2.isActive = true
                    AND d2.startDate <= :currentTime
                    AND d2.endDate >= :currentTime
                    AND (d2.product = p OR d2.category = c OR (d2.product IS NULL AND d2.category IS NULL AND d2.productVariant IS NULL))
                    AND (d2.minOrderValue IS NULL OR d2.minOrderValue <= COALESCE(MIN(pv.price), 0))
                  ORDER BY d2.discountValue DESC
                  LIMIT 1) AS discountAmount,
                                   (SELECT d2.discountType
                  FROM Discount d2
                  WHERE d2.isActive = true
                    AND d2.startDate <= :currentTime
                    AND d2.endDate >= :currentTime
                    AND (d2.product = p OR d2.category = c OR (d2.product IS NULL AND d2.category IS NULL AND d2.productVariant IS NULL))
                    AND (d2.minOrderValue IS NULL OR d2.minOrderValue <= COALESCE(MIN(pv.price), 0))
                  ORDER BY d2.discountValue DESC
                  LIMIT 1) AS discountType,
                                   (SELECT d2.name
                  FROM Discount d2
                  WHERE d2.isActive = true
                    AND d2.startDate <= :currentTime
                    AND d2.endDate >= :currentTime
                    AND (d2.product = p OR d2.category = c OR (d2.product IS NULL AND d2.category IS NULL AND d2.productVariant IS NULL))
                    AND (d2.minOrderValue IS NULL OR d2.minOrderValue <= COALESCE(MIN(pv.price), 0))
                  ORDER BY d2.discountValue DESC
                  LIMIT 1) AS discountName,
                                   (SELECT d2.startDate
                  FROM Discount d2
                  WHERE d2.isActive = true
                    AND d2.startDate <= :currentTime
                    AND d2.endDate >= :currentTime
                    AND (d2.product = p OR d2.category = c OR (d2.product IS NULL AND d2.category IS NULL AND d2.productVariant IS NULL))
                    AND (d2.minOrderValue IS NULL OR d2.minOrderValue <= COALESCE(MIN(pv.price), 0))
                  ORDER BY d2.discountValue DESC
                  LIMIT 1) AS discountStartDate,
                                   (SELECT d2.endDate
                  FROM Discount d2
                  WHERE d2.isActive = true
                    AND d2.startDate <= :currentTime
                    AND d2.endDate >= :currentTime
                    AND (d2.product = p OR d2.category = c OR (d2.product IS NULL AND d2.category IS NULL AND d2.productVariant IS NULL))
                    AND (d2.minOrderValue IS NULL OR d2.minOrderValue <= COALESCE(MIN(pv.price), 0))
                  ORDER BY d2.discountValue DESC
                  LIMIT 1) AS discountEndDate,
                                   (SELECT d2.minOrderValue
                  FROM Discount d2
                  WHERE d2.isActive = true
                    AND d2.startDate <= :currentTime
                    AND d2.endDate >= :currentTime
                    AND (d2.product = p OR d2.category = c OR (d2.product IS NULL AND d2.category IS NULL AND d2.productVariant IS NULL))
                    AND (d2.minOrderValue IS NULL OR d2.minOrderValue <= COALESCE(MIN(pv.price), 0))
                  ORDER BY d2.discountValue DESC
                  LIMIT 1) AS minOrderValue
          FROM Product p
          JOIN p.category c
          JOIN p.shop s
          LEFT JOIN p.productVariants pv ON pv.isActive = true
          WHERE p.isActive = true
                         AND EXISTS (
               SELECT 1 FROM Discount d3
               WHERE d3.isActive = true
                 AND d3.startDate <= :currentTime
                 AND d3.endDate >= :currentTime
                 AND (d3.product = p OR d3.category = c OR (d3.product IS NULL AND d3.category IS NULL AND d3.productVariant IS NULL))
                 AND (d3.minOrderValue IS NULL OR d3.minOrderValue <= (SELECT COALESCE(MIN(pv2.price), 0) FROM ProductVariant pv2 WHERE pv2.product = p AND pv2.isActive = true))
             )
          GROUP BY p.id, p.name, p.brand, p.description, p.productImage, p.isActive,
                   p.viewCount, p.soldCount, c.name, c.id, s.shopName
                                          ORDER BY (SELECT d2.discountValue
                     FROM Discount d2
                     WHERE d2.isActive = true
                       AND d2.startDate <= :currentTime
                       AND d2.endDate >= :currentTime
                       AND (d2.product = p OR d2.category = c OR (d2.product IS NULL AND d2.category IS NULL AND d2.productVariant IS NULL))
                       AND (d2.minOrderValue IS NULL OR d2.minOrderValue <= (SELECT COALESCE(MIN(pv2.price), 0) FROM ProductVariant pv2 WHERE pv2.product = p AND pv2.isActive = true))
                     ORDER BY 
                       CASE 
                         WHEN d2.discountType = 'PERCENTAGE' THEN 1
                         WHEN d2.discountType = 'FIXED' THEN 2
                         ELSE 3
                       END,
                       d2.discountValue DESC
                     LIMIT 1) DESC, p.viewCount DESC
      """)
  List<ProductDisplayDto> findActiveProductsWithActiveDiscounts(
      @Param("currentTime") java.time.OffsetDateTime currentTime);

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
              AND d.startDate <= :currentTime
              AND d.endDate >= :currentTime
              AND (
                  d.product = p
                  OR d.category = c
                  OR (d.product IS NULL AND d.category IS NULL AND d.productVariant IS NULL)
              )
          WHERE p.isActive = true
          GROUP BY p.id, p.name, p.brand, p.description, p.productImage, p.isActive,
                   p.viewCount, p.soldCount, c.name, c.id, s.shopName,
                   d.discountType, d.discountValue, d.name, d.startDate, d.endDate, d.minOrderValue
          ORDER BY 
            CASE 
              WHEN d.discountType = 'PERCENTAGE' THEN 1
              WHEN d.discountType = 'FIXED' THEN 2
              ELSE 3
            END,
            d.discountValue DESC, p.viewCount DESC
      """)
  List<ProductDisplayDto> findActiveProductsWithProductSpecificDiscounts(@Param("currentTime") OffsetDateTime currentTime);

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
              AND d.startDate <= :currentTime
              AND d.endDate >= :currentTime
              AND d.product = p
          WHERE p.isActive = true
          ORDER BY 
            CASE 
              WHEN d.discountType = 'PERCENTAGE' THEN 1
              WHEN d.discountType = 'FIXED' THEN 2
              ELSE 3
            END,
            d.discountValue DESC, p.viewCount DESC
      """)
  List<ProductDisplayDto> findActiveProductsWithDiscountsSimple(@Param("currentTime") OffsetDateTime currentTime);

  @Query("""
          SELECT p.id AS id,
                 p.name AS name,
                 p.brand AS brand,
                 p.description AS description,
                 p.productImage AS productImage,
                 p.isActive AS isActive,
                 c.name AS categoryName,
                 c.id AS categoryId
          FROM Product p
          JOIN p.category c
          WHERE p.id = :productId
      """)
  ProductDto findProductForEdit(@Param("productId") UUID productId);

  // Không cần method này nữa vì sẽ sử dụng Specification
}