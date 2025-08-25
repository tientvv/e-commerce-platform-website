package com.tientvv.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@Slf4j
public class PasswordStrengthService {

    public enum PasswordStrength {
        VERY_WEAK(1, "Rất yếu"),
        WEAK(2, "Yếu"),
        MEDIUM(3, "Trung bình"),
        STRONG(4, "Mạnh"),
        VERY_STRONG(5, "Rất mạnh");

        private final int level;
        private final String displayName;

        PasswordStrength(int level, String displayName) {
            this.level = level;
            this.displayName = displayName;
        }

        public int getLevel() {
            return level;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public static class PasswordStrengthResult {
        private final PasswordStrength strength;
        private final int score;
        private final String message;
        private final boolean isValid;

        public PasswordStrengthResult(PasswordStrength strength, int score, String message, boolean isValid) {
            this.strength = strength;
            this.score = score;
            this.message = message;
            this.isValid = isValid;
        }

        public PasswordStrength getStrength() {
            return strength;
        }

        public int getScore() {
            return score;
        }

        public String getMessage() {
            return message;
        }

        public boolean isValid() {
            return isValid;
        }
    }

    public PasswordStrengthResult checkPasswordStrength(String password) {
        if (password == null || password.trim().isEmpty()) {
            return new PasswordStrengthResult(
                PasswordStrength.VERY_WEAK, 0, 
                "Mật khẩu không được để trống", false
            );
        }

        int score = 0;
        StringBuilder feedback = new StringBuilder();

        // Kiểm tra độ dài
        if (password.length() < 6) {
            feedback.append("Mật khẩu phải có ít nhất 6 ký tự. ");
        } else if (password.length() >= 8) {
            score += 2;
        } else {
            score += 1;
        }

        // Kiểm tra chữ hoa
        if (Pattern.compile("[A-Z]").matcher(password).find()) {
            score += 1;
        } else {
            feedback.append("Thêm chữ hoa để tăng độ mạnh. ");
        }

        // Kiểm tra chữ thường
        if (Pattern.compile("[a-z]").matcher(password).find()) {
            score += 1;
        } else {
            feedback.append("Thêm chữ thường để tăng độ mạnh. ");
        }

        // Kiểm tra số
        if (Pattern.compile("\\d").matcher(password).find()) {
            score += 1;
        } else {
            feedback.append("Thêm số để tăng độ mạnh. ");
        }

        // Kiểm tra ký tự đặc biệt
        if (Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]").matcher(password).find()) {
            score += 1;
        } else {
            feedback.append("Thêm ký tự đặc biệt để tăng độ mạnh. ");
        }

        // Kiểm tra mật khẩu yếu phổ biến
        if (isCommonWeakPassword(password)) {
            score = Math.max(0, score - 2);
            feedback.insert(0, "Mật khẩu này quá phổ biến và dễ đoán. ");
        }

        // Xác định độ mạnh dựa trên điểm số
        PasswordStrength strength;
        boolean isValid;
        String finalMessage;

        if (score <= 1) {
            strength = PasswordStrength.VERY_WEAK;
            isValid = false;
            finalMessage = "Mật khẩu rất yếu. " + feedback.toString();
        } else if (score == 2) {
            strength = PasswordStrength.WEAK;
            isValid = false;
            finalMessage = "Mật khẩu yếu. " + feedback.toString();
        } else if (score == 3) {
            strength = PasswordStrength.MEDIUM;
            isValid = true;
            finalMessage = "Mật khẩu trung bình. " + feedback.toString();
        } else if (score == 4) {
            strength = PasswordStrength.STRONG;
            isValid = true;
            finalMessage = "Mật khẩu mạnh. " + feedback.toString();
        } else {
            strength = PasswordStrength.VERY_STRONG;
            isValid = true;
            finalMessage = "Mật khẩu rất mạnh! " + feedback.toString();
        }

        return new PasswordStrengthResult(strength, score, finalMessage, isValid);
    }

    private boolean isCommonWeakPassword(String password) {
        String[] commonPasswords = {
            "123456", "password", "123456789", "12345678", "12345", "qwerty",
            "abc123", "111111", "123123", "admin", "letmein", "welcome",
            "monkey", "1234567", "1234567890", "password123", "12345678910",
            "123456789a", "123456789ab", "123456789abc", "123456789abcd",
            "123456789abcde", "123456789abcdef", "123456789abcdefg",
            "123456789abcdefgh", "123456789abcdefghi", "123456789abcdefghij"
        };

        String lowerPassword = password.toLowerCase();
        for (String weakPassword : commonPasswords) {
            if (lowerPassword.equals(weakPassword)) {
                return true;
            }
        }

        // Kiểm tra mật khẩu chỉ có số liên tiếp
        if (Pattern.compile("^\\d+$").matcher(password).matches()) {
            return true;
        }

        // Kiểm tra mật khẩu chỉ có chữ cái liên tiếp
        if (Pattern.compile("^[a-zA-Z]+$").matcher(password).matches()) {
            return true;
        }

        return false;
    }
}
