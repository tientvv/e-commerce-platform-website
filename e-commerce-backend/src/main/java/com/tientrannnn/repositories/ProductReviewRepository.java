package com.tientrannnn.repositories;

import com.tientrannnn.models.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, UUID> {
    
    @Query("SELECT pr FROM ProductReview pr WHERE pr.product.id = :productId AND pr.isApproved = true")
    Page<ProductReview> findByProductIdApproved(@Param("productId") UUID productId, Pageable pageable);
    
    @Query("SELECT pr FROM ProductReview pr WHERE pr.user.id = :userId")
    Page<ProductReview> findByUserId(@Param("userId") UUID userId, Pageable pageable);
    
    @Query("SELECT pr FROM ProductReview pr WHERE pr.product.id = :productId AND pr.user.id = :userId")
    ProductReview findByProductIdAndUserId(@Param("productId") UUID productId, @Param("userId") UUID userId);
    
    @Query("SELECT AVG(pr.rating) FROM ProductReview pr WHERE pr.product.id = :productId AND pr.isApproved = true")
    Double findAverageRatingByProductId(@Param("productId") UUID productId);
    
    @Query("SELECT COUNT(pr) FROM ProductReview pr WHERE pr.product.id = :productId AND pr.isApproved = true")
    Long countByProductId(@Param("productId") UUID productId);
}