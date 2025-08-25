package com.tientvv.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class EncodingUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(EncodingUtils.class);
    
    /**
     * Xử lý encoding UTF-8 cho chuỗi tiếng Việt
     * @param input Chuỗi đầu vào có thể bị lỗi encoding
     * @return Chuỗi đã được xử lý encoding đúng
     */
    public static String fixVietnameseEncoding(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }
        
        try {
            logger.debug("Original string: '{}'", input);
            logger.debug("Original bytes: {}", java.util.Arrays.toString(input.getBytes(StandardCharsets.UTF_8)));
            
            String result = input;
            
            // Kiểm tra các dấu hiệu encoding bị lỗi
            if (hasEncodingIssues(input)) {
                // Thử nhiều phương pháp decode khác nhau
                result = tryMultipleDecodings(input);
            }
            
            // Loại bỏ các ký tự không hợp lệ
            result = result.replaceAll("[\\x00-\\x1F\\x7F]", "");
            result = result.trim();
            
            logger.debug("Fixed string: '{}'", result);
            logger.debug("Fixed bytes: {}", java.util.Arrays.toString(result.getBytes(StandardCharsets.UTF_8)));
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error fixing encoding: {}", e.getMessage());
            return input;
        }
    }
    
    /**
     * Thử nhiều phương pháp decode khác nhau
     */
    private static String tryMultipleDecodings(String input) {
        // Danh sách các encoding để thử
        String[] encodings = {
            "ISO-8859-1",
            "Windows-1252", 
            "ISO-8859-15",
            "Cp1252",
            "UTF-8"
        };
        
        for (String encoding : encodings) {
            try {
                byte[] bytes = input.getBytes(encoding);
                String decoded = new String(bytes, StandardCharsets.UTF_8);
                
                // Kiểm tra xem kết quả có hợp lý không
                if (!hasEncodingIssues(decoded) && decoded.length() > 0) {
                    logger.info("Successfully fixed encoding using: {}", encoding);
                    return decoded;
                }
            } catch (Exception e) {
                logger.debug("Failed to decode using {}: {}", encoding, e.getMessage());
            }
        }
        
        logger.warn("Could not fix encoding with any method, using original string");
        return input;
    }
    
    /**
     * Kiểm tra xem chuỗi có bị lỗi encoding không
     * @param input Chuỗi cần kiểm tra
     * @return true nếu có dấu hiệu lỗi encoding
     */
    public static boolean hasEncodingIssues(String input) {
        if (input == null) return false;
        
        return input.contains("á»") || input.contains("á»") || input.contains("á»") ||
               input.contains("á»") || input.contains("á»") || input.contains("á»");
    }
    
    /**
     * Đảm bảo chuỗi được encode đúng UTF-8
     * @param input Chuỗi đầu vào
     * @return Chuỗi đã được encode đúng UTF-8
     */
    public static String ensureUtf8(String input) {
        if (input == null) return "";
        
        try {
            // Chuyển đổi sang UTF-8
            byte[] utf8Bytes = input.getBytes(StandardCharsets.UTF_8);
            return new String(utf8Bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("Error ensuring UTF-8 encoding: {}", e.getMessage());
            return input;
        }
    }
}
