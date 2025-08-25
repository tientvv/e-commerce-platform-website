package com.tientvv.controller;

import com.tientvv.dto.account.ForgotPasswordDto;
import com.tientvv.dto.account.ResetPasswordDto;
import com.tientvv.service.ResetPasswordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class ResetPasswordController {

    private final ResetPasswordService resetPasswordService;

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody ForgotPasswordDto forgotPasswordDto) {
        try {
            log.info("Received forgot password request for email: {}", forgotPasswordDto.getEmail());
            
            Map<String, Object> result = resetPasswordService.forgotPassword(forgotPasswordDto);
            
            if ((Boolean) result.get("success")) {
                log.info("Forgot password request processed successfully for email: {}", forgotPasswordDto.getEmail());
                return ResponseEntity.ok(result);
            } else {
                log.warn("Forgot password request failed for email: {} - {}", forgotPasswordDto.getEmail(), result.get("message"));
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (Exception e) {
            log.error("Error processing forgot password request for email {}: {}", forgotPasswordDto.getEmail(), e.getMessage(), e);
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "message", "Có lỗi xảy ra. Vui lòng thử lại sau"
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody ResetPasswordDto resetPasswordDto) {
        try {
            log.info("Received reset password request with token: {}", resetPasswordDto.getToken());
            
            Map<String, Object> result = resetPasswordService.resetPassword(resetPasswordDto);
            
            if ((Boolean) result.get("success")) {
                log.info("Password reset successfully for token: {}", resetPasswordDto.getToken());
                return ResponseEntity.ok(result);
            } else {
                log.warn("Password reset failed for token: {} - {}", resetPasswordDto.getToken(), result.get("message"));
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (Exception e) {
            log.error("Error processing reset password request for token {}: {}", resetPasswordDto.getToken(), e.getMessage(), e);
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "message", "Có lỗi xảy ra. Vui lòng thử lại sau"
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/verify-token")
    public ResponseEntity<Map<String, Object>> verifyToken(@RequestParam String token) {
        try {
            log.info("Verifying reset password token: {}", token);
            
            // Tạo DTO tạm thời để kiểm tra token
            ResetPasswordDto tempDto = new ResetPasswordDto();
            tempDto.setToken(token);
            tempDto.setNewPassword("temp");
            tempDto.setConfirmPassword("temp");
            
            Map<String, Object> result = resetPasswordService.resetPassword(tempDto);
            
            // Nếu token hợp lệ (không phải lỗi token), trả về success
            if (result.get("message").toString().contains("Mã xác nhận")) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Token hợp lệ"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", result.get("message")
                ));
            }
            
        } catch (Exception e) {
            log.error("Error verifying token {}: {}", token, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra khi xác minh token"
            ));
        }
    }

    @PostMapping("/check-password-strength")
    public ResponseEntity<Map<String, Object>> checkPasswordStrength(@RequestBody Map<String, String> request) {
        try {
            String password = request.get("password");
            log.info("Checking password strength");
            
            Map<String, Object> result = resetPasswordService.checkPasswordStrength(password);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error checking password strength: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra khi kiểm tra độ mạnh mật khẩu"
            ));
        }
    }
}
