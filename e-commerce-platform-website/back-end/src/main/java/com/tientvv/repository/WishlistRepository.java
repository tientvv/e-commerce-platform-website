package com.tientvv.repository;

import com.tientvv.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, UUID> {

    // Tìm tất cả wishlist items của một account
    @Query("SELECT w FROM Wishlist w WHERE w.account.id = :accountId")
    List<Wishlist> findByAccountId(@Param("accountId") UUID accountId);

    // Tìm wishlist item theo account và product
    @Query("SELECT w FROM Wishlist w WHERE w.account.id = :accountId AND w.product.id = :productId")
    Optional<Wishlist> findByAccountIdAndProductId(@Param("accountId") UUID accountId, @Param("productId") UUID productId);

    // Tìm wishlist item theo account, product và variant
    @Query("SELECT w FROM Wishlist w WHERE w.account.id = :accountId AND w.product.id = :productId AND w.productVariant.id = :variantId")
    Optional<Wishlist> findByAccountIdAndProductIdAndVariantId(@Param("accountId") UUID accountId, @Param("productId") UUID productId, @Param("variantId") UUID variantId);

    // Kiểm tra xem product có trong wishlist của account không
    @Query("SELECT COUNT(w) > 0 FROM Wishlist w WHERE w.account.id = :accountId AND w.product.id = :productId")
    boolean existsByAccountIdAndProductId(@Param("accountId") UUID accountId, @Param("productId") UUID productId);

    // Kiểm tra xem product variant có trong wishlist của account không
    @Query("SELECT COUNT(w) > 0 FROM Wishlist w WHERE w.account.id = :accountId AND w.product.id = :productId AND w.productVariant.id = :variantId")
    boolean existsByAccountIdAndProductIdAndVariantId(@Param("accountId") UUID accountId, @Param("productId") UUID productId, @Param("variantId") UUID variantId);

    // Xóa wishlist item theo account và product
    @Modifying
    @Transactional
    @Query("DELETE FROM Wishlist w WHERE w.account.id = :accountId AND w.product.id = :productId")
    void deleteByAccountIdAndProductId(@Param("accountId") UUID accountId, @Param("productId") UUID productId);

    // Xóa wishlist item theo account, product và variant
    @Modifying
    @Transactional
    @Query("DELETE FROM Wishlist w WHERE w.account.id = :accountId AND w.product.id = :productId AND w.productVariant.id = :variantId")
    void deleteByAccountIdAndProductIdAndVariantId(@Param("accountId") UUID accountId, @Param("productId") UUID productId, @Param("variantId") UUID variantId);

    // Đếm số lượng wishlist cho một sản phẩm
    @Query("SELECT COUNT(w) FROM Wishlist w WHERE w.product.id = :productId")
    long countByProductId(@Param("productId") UUID productId);

    // Đếm số lượng wishlist của một account
    @Query("SELECT COUNT(w) FROM Wishlist w WHERE w.account.id = :accountId")
    long countByAccountId(@Param("accountId") UUID accountId);
}
