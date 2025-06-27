package com.tientrannnn.repositories;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.tientrannnn.models.Shop;

public interface ShopRepository extends JpaRepository<Shop, UUID> {
  boolean existsByUserId(UUID id);
}
