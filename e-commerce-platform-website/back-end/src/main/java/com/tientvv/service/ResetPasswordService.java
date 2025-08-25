package com.tientvv.service;

import com.tientvv.dto.account.ForgotPasswordDto;
import com.tientvv.dto.account.ResetPasswordDto;
import com.tientvv.model.Account;
import com.tientvv.model.ResetPasswordToken;
import com.tientvv.repository.AccountRepository;
import com.tientvv.repository.ResetPasswordTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResetPasswordService {

    private final AccountRepository accountRepository;
    private final ResetPasswordTokenRepository resetPasswordTokenRepository;
    private final EmailService emailService;
    private final PasswordStrengthService passwordStrengthService;
    
    private static final int TOKEN_LENGTH = 6;
    private static final int TOKEN_EXPIRY_MINUTES = 10;
    private static final int MAX_REQUESTS_PER_HOUR = 3;
    
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public Map<String, Object> forgotPassword(ForgotPasswordDto forgotPasswordDto) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String email = forgotPasswordDto.getEmail();
            
            if (email == null || email.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Email không được để trống");
                return response;
            }
            
            // Tìm tài khoản theo email
            Account account = accountRepository.findByEmail(email);
            if (account == null) {
                response.put("success", false);
                response.put("message", "Email không tồn tại trong hệ thống");
                return response;
            }
            
            // Kiểm tra tài khoản có active không
            if (!account.getIsActive()) {
                response.put("success", false);
                response.put("message", "Tài khoản đã bị khóa");
                return response;
            }
            
            // Kiểm tra số lượng yêu cầu gần đây
            OffsetDateTime oneHourAgo = OffsetDateTime.now().minusHours(1);
            long recentRequests = resetPasswordTokenRepository.countRecentTokensByAccountId(account.getId(), oneHourAgo);
            
            if (recentRequests >= MAX_REQUESTS_PER_HOUR) {
                response.put("success", false);
                response.put("message", "Bạn đã yêu cầu đặt lại mật khẩu quá nhiều lần. Vui lòng thử lại sau 1 giờ");
                return response;
            }
            
            // Tạo token mới
            String token = generateToken();
            OffsetDateTime expiryDate = OffsetDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES);
            
            // Lưu token vào database
            ResetPasswordToken resetToken = new ResetPasswordToken();
            resetToken.setToken(token);
            resetToken.setAccount(account);
            resetToken.setExpiryDate(expiryDate);
            resetToken.setIsUsed(false);
            resetToken.setCreatedAt(OffsetDateTime.now());
            
            resetPasswordTokenRepository.save(resetToken);
            
            // Gửi email chứa mã code
            emailService.sendResetPasswordEmail(account.getEmail(), account.getName(), token);
            
            log.info("Reset password token created for account: {}", account.getEmail());
            
            response.put("success", true);
            response.put("message", "Mã xác nhận đã được gửi đến email của bạn");
            
        } catch (Exception e) {
            log.error("Error in forgotPassword: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra. Vui lòng thử lại sau");
        }
        
        return response;
    }

    @Transactional
    public Map<String, Object> resetPassword(ResetPasswordDto resetPasswordDto) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String token = resetPasswordDto.getToken();
            String newPassword = resetPasswordDto.getNewPassword();
            String confirmPassword = resetPasswordDto.getConfirmPassword();
            
            // Validate input
            if (token == null || token.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Mã xác nhận không được để trống");
                return response;
            }
            
            if (newPassword == null || newPassword.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Mật khẩu mới không được để trống");
                return response;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                response.put("success", false);
                response.put("message", "Mật khẩu xác nhận không khớp");
                return response;
            }
            
            // Kiểm tra độ mạnh mật khẩu
            PasswordStrengthService.PasswordStrengthResult strengthResult = 
                passwordStrengthService.checkPasswordStrength(newPassword);
            
            if (!strengthResult.isValid()) {
                response.put("success", false);
                response.put("message", strengthResult.getMessage());
                response.put("strength", strengthResult.getStrength().name());
                response.put("score", strengthResult.getScore());
                return response;
            }
            
            // Tìm token
            Optional<ResetPasswordToken> tokenOpt = resetPasswordTokenRepository.findByToken(token);
            if (tokenOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Mã xác nhận không hợp lệ");
                return response;
            }
            
            ResetPasswordToken resetToken = tokenOpt.get();
            
            // Kiểm tra token đã được sử dụng chưa
            if (resetToken.getIsUsed()) {
                response.put("success", false);
                response.put("message", "Mã xác nhận đã được sử dụng");
                return response;
            }
            
            // Kiểm tra token có hết hạn chưa
            if (resetToken.getExpiryDate().isBefore(OffsetDateTime.now())) {
                response.put("success", false);
                response.put("message", "Mã xác nhận đã hết hạn");
                return response;
            }
            
            // Cập nhật mật khẩu
            Account account = resetToken.getAccount();
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            account.setPassword(hashedPassword);
            account.setUpdatedAt(OffsetDateTime.now());
            
            accountRepository.save(account);
            
            // Đánh dấu token đã sử dụng
            resetToken.setIsUsed(true);
            resetToken.setUsedAt(OffsetDateTime.now());
            resetPasswordTokenRepository.save(resetToken);
            
            log.info("Password reset successfully for account: {} with strength: {}", 
                    account.getEmail(), strengthResult.getStrength().getDisplayName());
            
            response.put("success", true);
            response.put("message", "Đặt lại mật khẩu thành công");
            response.put("strength", strengthResult.getStrength().name());
            response.put("score", strengthResult.getScore());
            
        } catch (Exception e) {
            log.error("Error in resetPassword: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra. Vui lòng thử lại sau");
        }
        
        return response;
    }

    /**
     * API để kiểm tra độ mạnh mật khẩu (cho frontend sử dụng)
     */
    public Map<String, Object> checkPasswordStrength(String password) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            PasswordStrengthService.PasswordStrengthResult result = 
                passwordStrengthService.checkPasswordStrength(password);
            
            response.put("success", true);
            response.put("strength", result.getStrength().name());
            response.put("displayName", result.getStrength().getDisplayName());
            response.put("score", result.getScore());
            response.put("level", result.getStrength().getLevel());
            response.put("message", result.getMessage());
            response.put("isValid", result.isValid());
            
        } catch (Exception e) {
            log.error("Error checking password strength: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi kiểm tra độ mạnh mật khẩu");
        }
        
        return response;
    }

    private String generateToken() {
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            token.append(random.nextInt(10));
        }
        return token.toString();
    }
}
