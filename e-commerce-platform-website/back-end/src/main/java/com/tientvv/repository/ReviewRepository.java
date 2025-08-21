package com.tientvv.repository;

import com.tientvv.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByProductIdOrderByReviewDateDesc(UUID productId);

    @Query("SELECT r FROM Review r WHERE r.account.id = :accountId AND r.product.id = :productId")
    Review findByAccountAndProduct(@Param("accountId") UUID accountId, @Param("productId") UUID productId);

    @Query("SELECT COUNT(r) > 0 FROM Review r WHERE r.account.id = :accountId AND r.product.id = :productId")
    boolean existsByAccountAndProduct(@Param("accountId") UUID accountId, @Param("productId") UUID productId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    BigDecimal getAverageRatingByProductId(@Param("productId") UUID productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId")
    long countByProductId(@Param("productId") UUID productId);
}
