package com.tientvv.service;

import com.tientvv.dto.account.GoogleLoginDto;
import com.tientvv.model.Account;
import com.tientvv.repository.AccountRepository;
import com.tientvv.utils.EncodingUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
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
        logger.info("Received Google name: '{}'", googleLoginDto.getName());
        
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
                String sanitizedName = sanitizeName(googleLoginDto.getName());
                existingAccount.setName(sanitizedName != null ? sanitizedName : existingAccount.getName());
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
                String sanitizedName = sanitizeName(googleLoginDto.getName());
                accountWithEmail.setName(sanitizedName != null ? sanitizedName : accountWithEmail.getName());
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
            String sanitizedName = sanitizeName(googleLoginDto.getName());
            newAccount.setName(sanitizedName != null ? sanitizedName : "");
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

    /**
     * Xử lý encoding UTF-8 cho tên người dùng từ Google OAuth
     */
    private String sanitizeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "";
        }
        
        try {
            logger.info("Original name: '{}'", name);
            
            // Sử dụng EncodingUtils để xử lý encoding
            String sanitizedName = EncodingUtils.fixVietnameseEncoding(name);
            
            logger.info("Sanitized name: '{}'", sanitizedName);
            
            return sanitizedName;
        } catch (Exception e) {
            logger.error("Error sanitizing name: {}", e.getMessage());
            return name;
        }
    }
}
