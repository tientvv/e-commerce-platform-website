package com.tientrannnn.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.tientrannnn.models.Shop;

public interface ShopRepository extends JpaRepository<Shop, UUID> {
  Optional<Shop> findByUserId(UUID userId);

  boolean existsByUserId(UUID id);

  List<Shop> findByIsDeletedFalse();

  boolean existsByCccd(String cccd);
}