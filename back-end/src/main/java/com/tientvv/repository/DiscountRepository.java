package com.tientvv.repository;

import com.tientvv.model.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, UUID> {

    // Find all active discounts
    List<Discount> findByIsActiveTrueOrderByStartDateDesc();

    // Find all discounts (active and inactive)
    List<Discount> findAllByOrderByStartDateDesc();

    // Check if discount name exists
    boolean existsByNameAndIdNot(String name, UUID id);

    boolean existsByName(String name);

    // Find active discounts by date range
    @Query("SELECT d FROM Discount d WHERE d.isActive = true AND d.startDate <= :currentDate AND d.endDate >= :currentDate")
    List<Discount> findActiveDiscountsByDate(@Param("currentDate") OffsetDateTime currentDate);

    // Find discounts by product
    @Query("SELECT d FROM Discount d WHERE d.isActive = true AND (d.product.id = :productId OR d.category.id = :categoryId OR (d.product IS NULL AND d.category IS NULL AND d.productVariant IS NULL))")
    List<Discount> findApplicableDiscountsForProduct(@Param("productId") UUID productId,
            @Param("categoryId") UUID categoryId);

    // Find discounts by category
    @Query("SELECT d FROM Discount d WHERE d.isActive = true AND (d.category.id = :categoryId OR (d.product IS NULL AND d.category IS NULL AND d.productVariant IS NULL))")
    List<Discount> findApplicableDiscountsForCategory(@Param("categoryId") UUID categoryId);

    // Find discounts by product variant
    @Query("SELECT d FROM Discount d WHERE d.isActive = true AND (d.productVariant.id = :variantId OR d.product.id = :productId OR d.category.id = :categoryId OR (d.product IS NULL AND d.category IS NULL AND d.productVariant IS NULL))")
    List<Discount> findApplicableDiscountsForVariant(@Param("variantId") UUID variantId,
            @Param("productId") UUID productId, @Param("categoryId") UUID categoryId);

    // Find discount by name and active status
    java.util.Optional<Discount> findByNameAndIsActiveTrue(String name);
}