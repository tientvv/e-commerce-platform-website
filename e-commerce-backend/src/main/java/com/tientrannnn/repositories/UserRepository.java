package com.tientrannnn.repositories;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.tientrannnn.models.User;

public interface UserRepository extends JpaRepository<User, UUID> {
  User findByEmail(String email);

  // Check if a user exists by email to create a new user for Database
  boolean existsByEmail(String email);
}
