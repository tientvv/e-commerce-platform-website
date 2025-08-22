package com.tientvv.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tientvv.model.Account;

import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
  boolean existsByEmail(String email);

  boolean existsByUsername(String username);

  boolean existsByPhone(String phone);

  Account findByUsernameAndIsActive(String username, boolean isActive);

  Account findByUsername(String username);
  
  Account findByGoogleId(String googleId);
  
  Account findByEmail(String email);
}