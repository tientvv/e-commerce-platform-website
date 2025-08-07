package com.tientvv.repository;

import com.tientvv.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("unused")
@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

  List<Payment> findByIsActiveTrueOrderByPaymentName();

  List<Payment> findAllByOrderByPaymentName();

  Optional<Payment> findByPaymentCodeAndIsActiveTrue(String paymentCode);

  boolean existsByPaymentCode(String paymentCode);

  boolean existsByPaymentCodeAndIdNot(String paymentCode, UUID id);
}