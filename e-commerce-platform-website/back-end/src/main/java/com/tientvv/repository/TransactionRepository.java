package com.tientvv.repository;

import com.tientvv.model.Transaction;
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
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

	@Query("SELECT t FROM Transaction t WHERE t.order.id = :orderId")
	List<Transaction> findByOrderId(@Param("orderId") UUID orderId);

	@Query("SELECT t FROM Transaction t WHERE t.transactionCode = :transactionCode")
	Optional<Transaction> findByTransactionCode(@Param("transactionCode") String transactionCode);

	@Query("SELECT t FROM Transaction t WHERE t.transactionStatus = :status")
	List<Transaction> findByTransactionStatus(@Param("status") String status);

	@Query("SELECT t FROM Transaction t WHERE t.order.shop.id = :shopId")
	List<Transaction> findByShopId(@Param("shopId") UUID shopId);

	@Query(value = "SELECT COALESCE(SUM(t.transaction_amount), 0) FROM transactions t " +
			"JOIN orders o ON o.id = t.order_id " +
			"WHERE o.shop_id = :shopId AND t.transaction_status = :status " +
			"AND (:startDate IS NULL OR CAST(t.transaction_date AS date) >= :startDate) " +
			"AND (:endDate IS NULL OR CAST(t.transaction_date AS date) <= :endDate)", nativeQuery = true)
	BigDecimal getTotalRevenueByShopIdAndStatus(
			@Param("shopId") UUID shopId,
			@Param("status") String status,
			@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate);

	@Query(value = "SELECT COALESCE(SUM(oi.product_price * oi.quantity), 0) FROM order_items oi " +
			"JOIN orders o ON o.id = oi.order_id " +
			"JOIN transactions t ON t.order_id = o.id " +
			"WHERE o.shop_id = :shopId AND t.transaction_status = :status " +
			"AND (:startDate IS NULL OR CAST(t.transaction_date AS date) >= :startDate) " +
			"AND (:endDate IS NULL OR CAST(t.transaction_date AS date) <= :endDate)", nativeQuery = true)
	BigDecimal getTotalProductRevenueByShopIdAndStatus(
			@Param("shopId") UUID shopId,
			@Param("status") String status,
			@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate);

	@Query(value = "SELECT COUNT(DISTINCT t.order_id) FROM transactions t " +
			"JOIN orders o ON o.id = t.order_id " +
			"WHERE o.shop_id = :shopId AND t.transaction_status = 'SUCCESS' " +
			"AND (:startDate IS NULL OR CAST(t.transaction_date AS date) >= :startDate) " +
			"AND (:endDate IS NULL OR CAST(t.transaction_date AS date) <= :endDate)", nativeQuery = true)
	long countSuccessfulOrdersByShopId(
			@Param("shopId") UUID shopId,
			@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate);

	@Query(value = "SELECT CAST(t.transaction_date AS date) as date, " +
			"SUM(t.transaction_amount) as revenue, " +
			"COUNT(DISTINCT t.order_id) as orderCount " +
			"FROM transactions t " +
			"JOIN orders o ON o.id = t.order_id " +
			"WHERE o.shop_id = :shopId AND t.transaction_status = :status " +
			"AND (:startDate IS NULL OR CAST(t.transaction_date AS date) >= :startDate) " +
			"AND (:endDate IS NULL OR CAST(t.transaction_date AS date) <= :endDate) " +
			"GROUP BY CAST(t.transaction_date AS date) " +
			"ORDER BY date", nativeQuery = true)
	List<Object[]> getRevenueByPeriod(
			@Param("shopId") UUID shopId,
			@Param("status") String status,
			@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate);

	@Query(value = "SELECT CAST(t.transaction_date AS date) as date, " +
			"SUM(oi.product_price * oi.quantity) as revenue, " +
			"COUNT(DISTINCT t.order_id) as orderCount " +
			"FROM transactions t " +
			"JOIN orders o ON o.id = t.order_id " +
			"JOIN order_items oi ON oi.order_id = o.id " +
			"WHERE o.shop_id = :shopId AND t.transaction_status = :status " +
			"AND (:startDate IS NULL OR CAST(t.transaction_date AS date) >= :startDate) " +
			"AND (:endDate IS NULL OR CAST(t.transaction_date AS date) <= :endDate) " +
			"GROUP BY CAST(t.transaction_date AS date) " +
			"ORDER BY date", nativeQuery = true)
	List<Object[]> getProductRevenueByPeriod(
			@Param("shopId") UUID shopId,
			@Param("status") String status,
			@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate);
}