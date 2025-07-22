package com.tientvv.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.tientvv.model.Shop;

public interface ShopRepository extends JpaRepository<Shop, UUID> {
  Shop findByUserId(UUID userId);
}
