package com.tientrannnn.repositories;

import com.tientrannnn.models.Cart;
import com.tientrannnn.models.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {
    
    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId")
    Cart findByUserId(@Param("userId") UUID userId);
}

@Repository
interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId")
    List<CartItem> findByCartId(@Param("cartId") UUID cartId);
    
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.productVariant.id = :variantId")
    CartItem findByCartIdAndVariantId(@Param("cartId") UUID cartId, @Param("variantId") UUID variantId);
    
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.product.id = :productId AND ci.productVariant IS NULL")
    CartItem findByCartIdAndProductId(@Param("cartId") UUID cartId, @Param("productId") UUID productId);
    
    void deleteByCartIdAndProductVariantId(UUID cartId, UUID variantId);
    
    void deleteByCartId(UUID cartId);
}