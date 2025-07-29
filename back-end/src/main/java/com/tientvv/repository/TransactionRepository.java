package com.tientvv.repository;

import com.tientvv.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}