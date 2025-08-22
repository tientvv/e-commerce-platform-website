package com.tientvv.service;

import com.tientvv.dto.account.GoogleLoginDto;
import com.tientvv.model.Account;
import com.tientvv.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.UUID;

@SuppressWarnings("unused")
@Service
@Transactional
public class GoogleAuthService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleAuthService.class);

    @Autowired
    private AccountRepository accountRepository;

    public Account authenticateGoogleUser(GoogleLoginDto googleLoginDto) {
        logger.info("Starting Google authentication for email: {}", googleLoginDto.getEmail());
        
        // Validate input data
        if (googleLoginDto.getGoogleId() == null || googleLoginDto.getGoogleId().trim().isEmpty()) {
            throw new IllegalArgumentException("Google ID cannot be null or empty");
        }
        if (googleLoginDto.getEmail() == null || googleLoginDto.getEmail().trim().isEmpty()) {
            logger.error("Email is null or empty. GoogleLoginDto: {}", googleLoginDto);
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        
        try {
            // Kiểm tra xem tài khoản đã tồn tại với google_id này chưa
            Account existingAccount = accountRepository.findByGoogleId(googleLoginDto.getGoogleId());
            
            if (existingAccount != null) {
                logger.info("Found existing account with Google ID: {}", googleLoginDto.getGoogleId());
                // Cập nhật thông tin mới nhất từ Google
                existingAccount.setName(googleLoginDto.getName() != null ? googleLoginDto.getName() : existingAccount.getName());
                existingAccount.setEmail(googleLoginDto.getEmail());
                existingAccount.setAccountsImage(googleLoginDto.getPicture());
                existingAccount.setLastLogin(OffsetDateTime.now());
                existingAccount.setUpdatedAt(OffsetDateTime.now());
                
                Account savedAccount = accountRepository.save(existingAccount);
                logger.info("Updated existing account successfully");
                return savedAccount;
            }

            // Kiểm tra xem email đã được sử dụng bởi tài khoản khác chưa
            Account accountWithEmail = accountRepository.findByEmail(googleLoginDto.getEmail());
            if (accountWithEmail != null) {
                logger.info("Found existing account with email: {}", googleLoginDto.getEmail());
                // Nếu email đã tồn tại, liên kết google_id với tài khoản hiện có
                accountWithEmail.setGoogleId(googleLoginDto.getGoogleId());
                accountWithEmail.setName(googleLoginDto.getName() != null ? googleLoginDto.getName() : accountWithEmail.getName());
                accountWithEmail.setAccountsImage(googleLoginDto.getPicture());
                accountWithEmail.setLastLogin(OffsetDateTime.now());
                accountWithEmail.setUpdatedAt(OffsetDateTime.now());
                
                Account savedAccount = accountRepository.save(accountWithEmail);
                logger.info("Linked Google ID to existing account successfully");
                return savedAccount;
            }

            // Tạo tài khoản mới
            logger.info("Creating new account for Google user");
            Account newAccount = new Account();
            newAccount.setGoogleId(googleLoginDto.getGoogleId());
            newAccount.setUsername(generateUsername(googleLoginDto.getEmail()));
            newAccount.setName(googleLoginDto.getName() != null ? googleLoginDto.getName() : "");
            newAccount.setEmail(googleLoginDto.getEmail());
            newAccount.setAccountsImage(googleLoginDto.getPicture());
            newAccount.setPassword(""); // Set empty password for Google accounts
            newAccount.setPhone(""); // Set empty phone for Google accounts
            newAccount.setAddress(""); // Set empty address for Google accounts
            newAccount.setRole("USER");
            newAccount.setIsActive(true);
            newAccount.setCreatedAt(OffsetDateTime.now());
            newAccount.setUpdatedAt(OffsetDateTime.now());
            newAccount.setLastLogin(OffsetDateTime.now());

            logger.info("About to save new account with username: {}", newAccount.getUsername());
            Account savedAccount = accountRepository.save(newAccount);
            logger.info("Created new account successfully with ID: {}", savedAccount.getId());
            return savedAccount;
            
        } catch (Exception e) {
            logger.error("Error during Google authentication: {}", e.getMessage(), e);
            throw e;
        }
    }

    private String generateUsername(String email) {
        String baseUsername = email.split("@")[0];
        String username = baseUsername;
        int counter = 1;
        
        while (accountRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }
        
        return username;
    }
}
