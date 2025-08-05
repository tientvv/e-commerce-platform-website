package com.tientvv.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayOSService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${payos.client-id}")
    private String clientId;

    @Value("${payos.api-key}")
    private String apiKey;

    @Value("${payos.checksum-key}")
    private String checksumKey;

    @Value("${payos.return-url}")
    private String returnUrl;

    @Value("${payos.cancel-url}")
    private String cancelUrl;

    @SuppressWarnings({ "rawtypes", "null", "unchecked" })
    public Map<String, Object> createPaymentUrl(String orderId, Long amount, String description) {
        try {
            log.info("Creating PayOS payment for orderId: {}, amount: {}", orderId, amount);

            // Validate input parameters
            if (orderId == null || orderId.trim().isEmpty()) {
                throw new IllegalArgumentException("orderId cannot be null or empty");
            }
            if (amount == null || amount <= 0) {
                throw new IllegalArgumentException("amount must be greater than 0");
            }
            if (description == null || description.trim().isEmpty()) {
                throw new IllegalArgumentException("description cannot be null or empty");
            }

            // Create request body according to PayOS API v2 documentation
            Map<String, Object> requestBody = new HashMap<>();
            
            // Convert orderId to integer if possible, otherwise use a hash
            int orderCodeInt;
            try {
                orderCodeInt = Integer.parseInt(orderId.replaceAll("[^0-9]", ""));
            } catch (NumberFormatException e) {
                // If orderId is not numeric, use hash of the string
                orderCodeInt = Math.abs(orderId.hashCode());
            }
            
            requestBody.put("orderCode", orderCodeInt);
            requestBody.put("amount", amount);
            
            // Giới hạn description tối đa 25 ký tự
            String shortDescription = description.length() > 25 ? description.substring(0, 25) : description;
            requestBody.put("description", shortDescription);
            
            requestBody.put("returnUrl", returnUrl);
            requestBody.put("cancelUrl", cancelUrl);
            
            // expiredAt phải là int32 (nhỏ hơn 2147483647)
            long expiredAt = (System.currentTimeMillis() / 1000) + 900; // 15 phút sau, tính bằng seconds
            requestBody.put("expiredAt", (int) expiredAt);
            
            // Generate signature with the same orderCode value and short description
            String signature = generateSignature(String.valueOf(orderCodeInt), amount, shortDescription);
            requestBody.put("signature", signature);

            log.info("PayOS request body: {}", objectMapper.writeValueAsString(requestBody));

            // Call PayOS API
            String url = "https://api-merchant.payos.vn/v2/payment-requests";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-client-id", clientId);
            headers.set("x-api-key", apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            log.info("PayOS response status: {}", response.getStatusCode());
            log.info("PayOS response body: {}", response.getBody());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                
                // Parse response code safely
                Integer code = parseResponseCode(responseBody.get("code"));
                String desc = (String) responseBody.get("desc");
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");

                if (code != null && code == 0 && data != null) {
                    // Success
                    String paymentUrl = (String) data.get("checkoutUrl");
                    String transactionCode = (String) data.get("transactionCode");

                    Map<String, Object> successResponse = new HashMap<>();
                    successResponse.put("success", true);
                    successResponse.put("paymentUrl", paymentUrl);
                    successResponse.put("orderCode", String.valueOf(orderCodeInt));
                    successResponse.put("amount", amount);
                    successResponse.put("transactionCode", transactionCode);
                    successResponse.put("message", "Tạo URL thanh toán PayOS thành công");

                    log.info("PayOS payment URL created successfully: {}", paymentUrl);
                    return successResponse;
                } else {
                    // Error from PayOS
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", "PayOS error: " + (desc != null ? desc : "Lỗi không xác định"));
                    errorResponse.put("errorCode", "PAYOS_ERROR");
                    errorResponse.put("payosCode", code);
                    return errorResponse;
                }
            } else {
                // HTTP error
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Lỗi kết nối PayOS API - Status: " + response.getStatusCode());
                errorResponse.put("errorCode", "PAYOS_CONNECTION_ERROR");
                return errorResponse;
            }

        } catch (Exception e) {
            log.error("Error creating PayOS payment: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi tạo thanh toán PayOS: " + e.getMessage());
            return errorResponse;
        }
    }

    public boolean verifyPayment(String orderCode, String transactionCode) {
        try {
            log.info("Verifying PayOS payment - orderCode: {}, transactionCode: {}", orderCode, transactionCode);

            // Call PayOS verify API
            String url = "https://api-merchant.payos.vn/v2/payment-requests/" + transactionCode;
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-client-id", clientId);
            headers.set("x-api-key", apiKey);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            log.info("PayOS verify response: {}", response.getBody());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = response.getBody();
                
                @SuppressWarnings("null")
                Integer code = parseResponseCode(responseBody.get("code"));
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");

                if (code != null && code == 0 && data != null) {
                    String status = (String) data.get("status");
                    if ("PAID".equals(status)) {
                        log.info("PayOS payment verified successfully for orderCode: {}", orderCode);
                        return true;
                    } else {
                        log.warn("PayOS payment not completed, status: {} for orderCode: {}", status, orderCode);
                        return false;
                    }
                } else {
                    log.error("PayOS verify error for orderCode: {}", orderCode);
                    return false;
                }
            } else {
                log.error("PayOS verify connection error for orderCode: {}", orderCode);
                return false;
            }

        } catch (Exception e) {
            log.error("Error verifying PayOS payment: ", e);
            return false;
        }
    }

    private String generateSignature(String orderCode, Long amount, String description) {
        try {
            // Ensure all values are not null
            if (orderCode == null || amount == null || description == null || returnUrl == null || cancelUrl == null) {
                throw new IllegalArgumentException("All parameters must not be null");
            }
            
            // Theo tài liệu PayOS API v2 chính thức: sort theo alphabet
            String message = "amount=" + amount + "&cancelUrl=" + cancelUrl + "&description=" + description + "&orderCode=" + orderCode + "&returnUrl=" + returnUrl;
            
            log.info("=== SIGNATURE GENERATION (PAYOS OFFICIAL) ===");
            log.info("orderCode: '{}'", orderCode);
            log.info("amount: {}", amount);
            log.info("description: '{}'", description);
            log.info("returnUrl: '{}'", returnUrl);
            log.info("cancelUrl: '{}'", cancelUrl);
            log.info("Message to hash (PAYOS OFFICIAL): '{}'", message);
            log.info("Checksum key length: {}", checksumKey.length());

            // Generate HMAC-SHA256 signature
            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(checksumKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmacSha256.init(secretKey);

            byte[] hash = hmacSha256.doFinal(message.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            String signature = hexString.toString();
            log.info("Generated signature (PAYOS OFFICIAL): '{}'", signature);
            log.info("=== END SIGNATURE GENERATION ===");
            
            return signature;
        } catch (Exception e) {
            log.error("Error generating signature: ", e);
            throw new RuntimeException("Error generating signature", e);
        }
    }

    private Integer parseResponseCode(Object codeObj) {
        if (codeObj == null) {
            return null;
        }
        
        if (codeObj instanceof Integer) {
            return (Integer) codeObj;
        } else if (codeObj instanceof String) {
            try {
                return Integer.parseInt((String) codeObj);
            } catch (NumberFormatException e) {
                log.warn("Could not parse code as integer: {}", codeObj);
                return null;
            }
        } else if (codeObj instanceof Number) {
            return ((Number) codeObj).intValue();
        }
        
        return null;
    }

    public Map<String, Object> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("clientId", clientId);
        config.put("returnUrl", returnUrl);
        config.put("cancelUrl", cancelUrl);
        return config;
    }
} 