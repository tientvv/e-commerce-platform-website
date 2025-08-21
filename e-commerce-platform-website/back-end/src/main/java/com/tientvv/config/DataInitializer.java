package com.tientvv.config;

import java.time.OffsetDateTime;
import javax.sql.DataSource;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.tientvv.model.*;
import com.tientvv.repository.*;

@Component
public class DataInitializer implements CommandLineRunner {

  @Autowired
  private AccountRepository accountRepository;

  @SuppressWarnings("unused")
  @Autowired
  private DataSource dataSource;

  @Override
  public void run(String... args) throws Exception {
    initializeAdminAccount();
  }

  private void initializeAdminAccount() {
    try {
      // Check if admin account already exists
      if (accountRepository.findByUsernameAndIsActive("admin", true) != null) {
        System.out.println("✅ Admin account already exists, skipping initialization.");
        return;
      }

      // Create default admin account
      Account adminAccount = new Account();
      adminAccount.setUsername("admin");
      adminAccount.setName("Admin");
      adminAccount.setEmail(null);
      adminAccount.setPassword(BCrypt.hashpw("admin@123", BCrypt.gensalt()));
      adminAccount.setPhone(null);
      adminAccount.setRole("ADMIN");
      adminAccount.setIsActive(true);
      adminAccount.setCreatedAt(OffsetDateTime.now());
      adminAccount.setUpdatedAt(OffsetDateTime.now());

      accountRepository.save(adminAccount);
    } catch (Exception e) 
    {
      System.err.println("❌ Error creating admin account: " + e.getMessage());
    }
  }
}