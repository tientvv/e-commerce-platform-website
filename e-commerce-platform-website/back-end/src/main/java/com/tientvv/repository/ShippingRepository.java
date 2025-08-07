package com.tientvv.repository;

import com.tientvv.model.Shipping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ShippingRepository extends JpaRepository<Shipping, UUID> {

  @Query("SELECT s FROM Shipping s WHERE s.isActive = true ORDER BY s.shippingMethod")
  List<Shipping> findByIsActiveTrueOrderByShippingMethod();

  List<Shipping> findByIsActive(Boolean isActive);
}