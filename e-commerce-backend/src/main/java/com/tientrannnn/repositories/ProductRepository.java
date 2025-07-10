package com.tientrannnn.repositories;

import com.tientrannnn.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    
    @Query("SELECT p FROM Product p WHERE p.isDeleted = false")
    Page<Product> findAllActive(Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.isDeleted = false AND p.category.id = :categoryId")
    Page<Product> findByCategoryId(@Param("categoryId") UUID categoryId, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.isDeleted = false AND p.shop.id = :shopId")
    Page<Product> findByShopId(@Param("shopId") UUID shopId, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.isDeleted = false AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.isDeleted = false AND p.soldQuantity > 0 ORDER BY p.soldQuantity DESC")
    Page<Product> findFeaturedProducts(Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.isDeleted = false AND p.id = :id")
    Product findByIdActive(@Param("id") UUID id);
    
    @Query("SELECT p FROM Product p WHERE p.isDeleted = false AND p.shop.id = :shopId AND p.id = :productId")
    Product findByIdAndShopId(@Param("productId") UUID productId, @Param("shopId") UUID shopId);
}