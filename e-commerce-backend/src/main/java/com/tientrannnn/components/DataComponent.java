package com.tientrannnn.components;

import java.time.OffsetDateTime;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.tientrannnn.models.User;
import com.tientrannnn.repositories.UserRepository;

@Component
public class DataComponent implements CommandLineRunner {

  @Autowired
  private UserRepository userRepository;

  @Override
  public void run(String... args) {
    String adminAccount = "admin";
    boolean isAdminAccount = !userRepository.existsByEmail(adminAccount);
    String userAccount = "user";
    boolean isUserAccount = !userRepository.existsByEmail(userAccount);
    String sellerAccount = "seller";
    boolean isSellerAccount = !userRepository.existsByEmail(sellerAccount);

    if (isAdminAccount) {
      User user = new User();
      user.setEmail(adminAccount);
      user.setPassword(BCrypt.hashpw("admin123", BCrypt.gensalt()));
      user.setRole("ADMIN");
      user.setCreatedAt(OffsetDateTime.now());
      user.setUpdatedAt(OffsetDateTime.now());
      userRepository.save(user);
      System.out.println("Admin account created: " + adminAccount);
    } else {
      System.out.println("Admin already exists: " + adminAccount);
    }

    if (isUserAccount) {
      User user = new User();
      user.setEmail(userAccount);
      user.setPassword(BCrypt.hashpw("user123", BCrypt.gensalt()));
      user.setPhone("0123456789");
      user.setRole("USER");
      user.setIsSeller(false);
      user.setCreatedAt(OffsetDateTime.now());
      user.setUpdatedAt(OffsetDateTime.now());
      userRepository.save(user);
      System.out.println("User account created: " + userAccount);
    } else {
      System.out.println("User already exists: " + userAccount);
    }
    if (isSellerAccount) {
      User user = new User();
      user.setEmail(sellerAccount);
      user.setPassword(BCrypt.hashpw("seller123", BCrypt.gensalt()));
      user.setPhone("1234567890");
      user.setRole("USER");
      user.setIsSeller(true);
      user.setCreatedAt(OffsetDateTime.now());
      user.setUpdatedAt(OffsetDateTime.now());
      userRepository.save(user);
      System.out.println("Seller account created: " + userAccount);
    } else {
      System.out.println("Seller already exists: " + userAccount);
    }
  }
}
