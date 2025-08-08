package com.tientvv.repository;

import com.tientvv.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.*;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
	@Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId")
	List<OrderItem> findByOrderId(@Param("orderId") UUID orderId);

	@Query(value = "SELECT p.name as productName, " +
			"pv.variant_name as variantName, " +
			"pv.variant_value as variantValue, " +
			"SUM(oi.quantity) as totalQuantity, " +
			"SUM(oi.quantity * oi.product_price) as totalRevenue, " +
			"COUNT(DISTINCT oi.order_id) as orderCount " +
			"FROM order_items oi " +
			"JOIN orders o ON o.id = oi.order_id " +
			"JOIN product_variants pv ON pv.id = oi.product_variant_id " +
			"JOIN products p ON p.id = pv.product_id " +
			"WHERE o.shop_id = :shopId " +
			"AND EXISTS (SELECT 1 FROM transactions t WHERE t.order_id = o.id AND t.transaction_status = 'SUCCESS') " +
			"AND (:startDate IS NULL OR CAST(o.order_date AS date) >= :startDate) " +
			"AND (:endDate IS NULL OR CAST(o.order_date AS date) <= :endDate) " +
			"GROUP BY p.name, pv.variant_name, pv.variant_value " +
			"ORDER BY totalQuantity DESC", nativeQuery = true)
	List<Object[]> getTopSellingProducts(
			@Param("shopId") UUID shopId,
			@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate);
}