package com.tientvv.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tientvv.model.Account;

import java.util.List;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
  boolean existsByEmail(String email);

  boolean existsByUsername(String username);

  boolean existsByPhone(String phone);

  Account findByUsernameAndIsActive(String username, boolean isActive);

  Account findByUsername(String username);
  
  Account findByGoogleId(String googleId);
  
  Account findByEmail(String email);
  
  Account findByEmailAndIsActive(String email, boolean isActive);
  
  // Tìm kiếm người dùng theo username hoặc email (loại trừ ADMIN)
  @Query("SELECT a FROM Account a WHERE (a.username LIKE CONCAT('%', :searchTerm, '%') OR a.email LIKE CONCAT('%', :searchTerm, '%')) AND a.role != 'ADMIN'")
  List<Account> findByUsernameOrEmailContaining(@Param("searchTerm") String searchTerm);
  
  // Lấy tất cả người dùng (loại trừ ADMIN)
  @Query("SELECT a FROM Account a WHERE a.role != 'ADMIN'")
  List<Account> findAllExceptAdmin();
}