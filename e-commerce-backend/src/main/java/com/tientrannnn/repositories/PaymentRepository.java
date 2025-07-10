package com.tientrannnn.repositories;

import com.tientrannnn.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    
    @Query("SELECT p FROM Payment p WHERE p.order.id = :orderId")
    List<Payment> findByOrderId(@Param("orderId") UUID orderId);
    
    @Query("SELECT p FROM Payment p WHERE p.transactionId = :transactionId")
    Payment findByTransactionId(@Param("transactionId") String transactionId);
    
    @Query("SELECT p FROM Payment p WHERE p.order.id = :orderId AND p.status = :status")
    Payment findByOrderIdAndStatus(@Param("orderId") UUID orderId, @Param("status") Payment.PaymentStatus status);
}