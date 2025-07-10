package com.tientrannnn.repositories;

import com.tientrannnn.models.Order;
import com.tientrannnn.models.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    Page<Order> findByUserId(@Param("userId") UUID userId, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.shop.id = :shopId ORDER BY o.createdAt DESC")
    Page<Order> findByShopId(@Param("shopId") UUID shopId, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.orderNumber = :orderNumber")
    Order findByOrderNumber(@Param("orderNumber") String orderNumber);
    
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status = :status")
    Page<Order> findByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") Order.OrderStatus status, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.shop.id = :shopId AND o.status = :status")
    Page<Order> findByShopIdAndStatus(@Param("shopId") UUID shopId, @Param("status") Order.OrderStatus status, Pageable pageable);
}

@Repository
interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId")
    List<OrderItem> findByOrderId(@Param("orderId") UUID orderId);
}