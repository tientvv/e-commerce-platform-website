package com.tientrannnn.repositories;

import com.tientrannnn.models.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, UUID> {
    
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId")
    Page<Wishlist> findByUserId(@Param("userId") UUID userId, Pageable pageable);
    
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND w.product.id = :productId")
    Wishlist findByUserIdAndProductId(@Param("userId") UUID userId, @Param("productId") UUID productId);
    
    @Query("SELECT COUNT(w) FROM Wishlist w WHERE w.product.id = :productId")
    Long countByProductId(@Param("productId") UUID productId);
    
    void deleteByUserIdAndProductId(UUID userId, UUID productId);
}