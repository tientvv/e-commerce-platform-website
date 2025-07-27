package com.tientvv.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tientvv.dto.CrudProduct.ProductDto;
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

  @Query("SELECT p FROM Product p WHERE p.shop.id = :shopId AND p.isActive = true")
  List<Product> findAllByShopIdAndIsActiveTrue(@Param("shopId") UUID shopId);
}
