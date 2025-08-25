package com.tientvv.repository;

import com.tientvv.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.product.id = :productId ORDER BY cm.createdAt ASC")
    List<ChatMessage> findByProductIdOrderByCreatedAtAsc(@Param("productId") UUID productId);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.shop.id = :shopId ORDER BY cm.createdAt DESC")
    List<ChatMessage> findByShopIdOrderByCreatedAtDesc(@Param("shopId") UUID shopId);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.user.id = :userId ORDER BY cm.createdAt DESC")
    List<ChatMessage> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.product.id = :productId AND cm.user.id = :userId ORDER BY cm.createdAt ASC")
    List<ChatMessage> findByProductIdAndUserIdOrderByCreatedAtAsc(@Param("productId") UUID productId, @Param("userId") UUID userId);
    
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.shop.id = :shopId AND cm.isRead = false AND cm.messageType = 'CUSTOMER_MESSAGE'")
    Long countUnreadMessagesByShopId(@Param("shopId") UUID shopId);
    
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.user.id = :userId AND cm.isRead = false AND cm.messageType = 'SHOP_REPLY'")
    Long countUnreadMessagesByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.shop.id = :shopId AND cm.isRead = false AND cm.messageType = 'CUSTOMER_MESSAGE'")
    List<ChatMessage> findUnreadMessagesByShopId(@Param("shopId") UUID shopId);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.user.id = :userId AND cm.isRead = false AND cm.messageType = 'SHOP_REPLY'")
    List<ChatMessage> findUnreadMessagesByUserId(@Param("userId") UUID userId);
}
