package com.tientv.repository;

import com.tientv.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
  boolean existsByEmail(String email);

  boolean existsByUsername(String username);

  boolean existsByPhone(String phone);

  Account findByUsernameAndIsActive(String username, boolean isActive);

  Account findByUsername(String username);
}