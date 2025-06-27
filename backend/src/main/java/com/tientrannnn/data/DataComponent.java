package com.tientrannnn.data;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tientrannnn.models.User;
import com.tientrannnn.repositories.UserRepository;
import java.time.OffsetDateTime;

@Component
public class DataComponent implements CommandLineRunner {

  @Autowired
  private UserRepository userRepository;

  @Override
  @Transactional
  public void run(String... args) {
    String adminAccount = "admin";
    String userAccount = "user";
    boolean isAdminAccount = !userRepository.existsByEmail(adminAccount);
    // Check if the admin account exists
    // If not, create a default admin account
    boolean isUserAccount = !userRepository.existsByEmail(userAccount);
    if (isAdminAccount) {
      // Create a default admin account
      User user = new User();
      user.setEmail(adminAccount);
      user.setPassword(BCrypt.hashpw("admin123", BCrypt.gensalt()));
      user.setRole("ADMIN");
      user.setPhone("0000000000");
      user.setCreatedAt(OffsetDateTime.now());
      user.setUpdatedAt(OffsetDateTime.now());
      userRepository.save(user);
      System.out.println("Admin account created: " + adminAccount);
    } else {
      System.out.println("Admin already exists: " + adminAccount);
    }
    if (isUserAccount) {
      // Create a default user account
      User user = new User();
      user.setEmail(userAccount);
      user.setPassword(BCrypt.hashpw("user123", BCrypt.gensalt()));
      user.setPhone("0000000001");
      user.setRole("USER");
      user.setCreatedAt(OffsetDateTime.now());
      user.setUpdatedAt(OffsetDateTime.now());
      userRepository.save(user);
      System.out.println("User account created: " + userAccount);
    } else {
      System.out.println("Admin already exists: " + userAccount);
    }
  }
}
