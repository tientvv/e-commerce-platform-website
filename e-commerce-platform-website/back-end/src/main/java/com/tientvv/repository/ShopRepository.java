package com.tientvv.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.tientvv.model.Shop;
import com.tientvv.model.Account;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShopRepository extends JpaRepository<Shop, UUID> {
  @Query("SELECT s FROM Shop s WHERE s.user.id = :userId")
  Shop findByUserId(@Param("userId") UUID userId);

  @Query("SELECT s FROM Shop s WHERE s.user = :user")
  Shop findByUser(@Param("user") Account user);

  // Tìm kiếm cửa hàng theo tên
  @Query("SELECT s FROM Shop s WHERE s.shopName LIKE CONCAT('%', :shopName, '%')")
  List<Shop> findByShopNameContaining(@Param("shopName") String shopName);
}
