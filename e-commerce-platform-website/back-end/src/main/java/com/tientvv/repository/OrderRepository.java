package com.tientvv.repository;

import com.tientvv.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
  @Query("SELECT o FROM Order o WHERE o.account.id = :accountId ORDER BY o.orderDate DESC")
  List<Order> findByAccountIdOrderByOrderDateDesc(@Param("accountId") UUID accountId);

  @Query("SELECT o FROM Order o WHERE o.shop.id = :shopId ORDER BY o.orderDate DESC")
  List<Order> findByShopIdOrderByOrderDateDesc(@Param("shopId") UUID shopId);

  @Query("SELECT o FROM Order o ORDER BY o.orderDate DESC")
  List<Order> findAllOrderByOrderDateDesc();

  // Shop Order Management Methods
  @Query(value = "SELECT o.* FROM orders o " +
      "WHERE o.shop_id = :shopId AND " +
      "(:status IS NULL OR o.order_status = :status) AND " +
      "(:paymentMethod IS NULL OR o.payment_id IN (SELECT p.id FROM payments p WHERE p.payment_code = :paymentMethod)) AND " +
      "(:startDate IS NULL OR CAST(o.order_date AS date) >= :startDate) AND " +
      "(:endDate IS NULL OR CAST(o.order_date AS date) <= :endDate) " +
      "ORDER BY o.order_date DESC", nativeQuery = true)
  List<Order> findByShopIdWithFilters(
      @Param("shopId") UUID shopId,
      @Param("status") String status,
      @Param("paymentMethod") String paymentMethod,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);

  @Query("SELECT o FROM Order o WHERE o.id = :orderId AND o.shop.id = :shopId")
  Optional<Order> findByIdAndShopId(@Param("orderId") UUID orderId, @Param("shopId") UUID shopId);

  @Query("SELECT COUNT(o) FROM Order o WHERE o.shop.id = :shopId")
  long countByShopId(@Param("shopId") UUID shopId);

  @Query("SELECT COUNT(o) FROM Order o WHERE o.shop.id = :shopId AND o.orderStatus = :status")
  long countByShopIdAndOrderStatus(@Param("shopId") UUID shopId, @Param("status") String status);

  @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.shop.id = :shopId AND o.orderStatus = 'PAID'")
  BigDecimal getTotalRevenueByShopId(@Param("shopId") UUID shopId);

  // Payment sync methods
  List<Order> findByOrderStatus(String orderStatus);

  // Find by orderCode
  List<Order> findByOrderCode(String orderCode);
}