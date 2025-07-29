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

@SuppressWarnings("unused")
@Service
@RequiredArgsConstructor
@Slf4j
public class PayOSService {

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

  private final ObjectMapper objectMapper;
  private final RestTemplate restTemplate = new RestTemplate();

  // PayOS SDK instance
  private vn.payos.PayOS payOSInstance;

  @SuppressWarnings({ "null" })
  public Map<String, Object> createPaymentUrl(String orderId, Long amount, String description) {
    try {
      log.info("Creating PayOS payment URL for orderId: {}, amount: {}", orderId, amount);

      // Kiểm tra xem PayOS đã được cấu hình chưa
      if (isPayOSConfigured()) {
        return createPaymentUrlWithREST(orderId, amount, description);
      } else {
        // Trả về thông báo yêu cầu cấu hình PayOS
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "PayOS chưa được cấu hình. Vui lòng liên hệ admin để cấu hình API key.");
        errorResponse.put("errorCode", "PAYOS_NOT_CONFIGURED");
        return errorResponse;
      }
    } catch (Exception e) {
      log.error("Error creating PayOS payment URL: ", e);
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("message", "Lỗi tạo URL thanh toán: " + e.getMessage());
      return errorResponse;
    }
  }

  private boolean isPayOSConfigured() {
    return clientId != null && !clientId.isEmpty() &&
        !clientId.equals("your-client-id") &&
        apiKey != null && !apiKey.isEmpty() &&
        !apiKey.equals("your-api-key") &&
        checksumKey != null && !checksumKey.isEmpty() &&
        !checksumKey.equals("your-checksum-key");
  }

  @SuppressWarnings({ "unchecked", "null", "rawtypes" })
  private Map<String, Object> createPaymentUrlWithREST(String orderId, Long amount, String description) {
    try {
      // Tạo payment data theo PayOS API specification
      Map<String, Object> paymentData = new HashMap<>();
      paymentData.put("orderCode", orderId);
      paymentData.put("amount", amount);
      paymentData.put("description", description);
      paymentData.put("cancelUrl", cancelUrl);
      paymentData.put("returnUrl", returnUrl);
      paymentData.put("signature", generateSignature(orderId, amount, description));

      // Tạo request body
      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("clientId", clientId);
      requestBody.put("apiKey", apiKey);
      requestBody.put("paymentData", paymentData);

      log.info("PayOS payment request: {}", objectMapper.writeValueAsString(requestBody));

      // Gọi PayOS API thật
      String payosApiUrl = "https://api-merchant.payos.vn/v2/payment-requests";

      // Tạo HTTP headers
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.set("x-client-id", clientId);
      headers.set("x-api-key", apiKey);

      // Tạo HTTP entity
      HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

      // Gọi PayOS API
      ResponseEntity<Map> payosResponse = restTemplate.exchange(
          payosApiUrl,
          HttpMethod.POST,
          entity,
          Map.class);

      if (payosResponse.getStatusCode() == HttpStatus.OK && payosResponse.getBody() != null) {
        Map<String, Object> payosData = payosResponse.getBody();

        // Kiểm tra response từ PayOS
        if (payosData.containsKey("error") && (Integer) payosData.get("error") != 0) {
          log.error("PayOS API error: {}", payosData);
          Map<String, Object> errorResponse = new HashMap<>();
          errorResponse.put("success", false);
          errorResponse.put("message", "PayOS API error: " + payosData.get("message"));
          return errorResponse;
        }

        // Lấy data từ response
        Map<String, Object> data = (Map<String, Object>) payosData.get("data");
        if (data != null) {
          Map<String, Object> response = new HashMap<>();
          response.put("success", true);
          response.put("paymentUrl", data.get("checkoutUrl"));
          response.put("orderCode", data.get("orderCode"));
          response.put("amount", data.get("amount"));
          response.put("transactionCode", data.get("transactionCode"));
          return response;
        }
      }

      // Fallback nếu response không đúng format
      log.warn("Unexpected PayOS response format: {}", payosResponse.getBody());
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("message", "Unexpected response from PayOS");
      return errorResponse;

    } catch (Exception e) {
      log.error("Error calling PayOS API: ", e);
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("message", "Error calling PayOS API: " + e.getMessage());
      return errorResponse;
    }
  }

  public boolean verifyPaymentCallback(Map<String, Object> callbackData) {
    try {
      String orderCode = (String) callbackData.get("orderCode");
      Long amount = Long.valueOf(callbackData.get("amount").toString());
      String signature = (String) callbackData.get("signature");
      String status = (String) callbackData.get("status");

      // Verify signature
      String expectedSignature = generateSignature(orderCode, amount, "");
      if (!expectedSignature.equals(signature)) {
        log.error("Invalid PayOS signature");
        return false;
      }

      // Check if payment is successful
      return "PAID".equals(status);

    } catch (Exception e) {
      log.error("Error verifying PayOS callback: ", e);
      return false;
    }
  }

  @SuppressWarnings("null")
  public boolean verifyPayment(String orderCode, String transactionCode) {
    try {
      log.info("Verifying payment for orderCode: {}, transactionCode: {}", orderCode, transactionCode);

      // Gọi PayOS API để verify payment
      String payosVerifyUrl = "https://api-merchant.payos.vn/v2/transactions/" + transactionCode;

      // Tạo HTTP headers
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.set("x-client-id", clientId);
      headers.set("x-api-key", apiKey);

      // Tạo HTTP entity
      HttpEntity<String> entity = new HttpEntity<>(headers);

      try {
        // Gọi PayOS API
        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> payosResponse = restTemplate.exchange(
            payosVerifyUrl,
            HttpMethod.GET,
            entity,
            Map.class);

        if (payosResponse.getStatusCode() == HttpStatus.OK && payosResponse.getBody() != null) {
          @SuppressWarnings("unchecked")
          Map<String, Object> payosData = payosResponse.getBody();

          // Kiểm tra response từ PayOS
          if (payosData.containsKey("error") && (Integer) payosData.get("error") != 0) {
            log.error("PayOS verification error: {}", payosData);
            return false;
          }

          // Lấy data từ response
          @SuppressWarnings("unchecked")
          Map<String, Object> data = (Map<String, Object>) payosData.get("data");
          if (data != null) {
            String status = (String) data.get("status");
            String responseOrderCode = (String) data.get("orderCode");

            // Kiểm tra orderCode và status
            return orderCode.equals(responseOrderCode) && "PAID".equals(status);
          }
        }

        log.warn("Unexpected PayOS verification response: {}", payosResponse.getBody());
        return false;

      } catch (Exception e) {
        log.error("Error calling PayOS verification API: ", e);
        return false;
      }

    } catch (Exception e) {
      log.error("Error verifying payment with PayOS: ", e);
      return false;
    }
  }

  private String generateSignature(String orderCode, Long amount, String description) {
    try {
      String data = orderCode + "|" + amount + "|" + description;
      Mac mac = Mac.getInstance("HmacSHA256");
      SecretKeySpec secretKeySpec = new SecretKeySpec(checksumKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
      mac.init(secretKeySpec);
      byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

      StringBuilder hexString = new StringBuilder();
      for (byte b : hash) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) {
          hexString.append('0');
        }
        hexString.append(hex);
      }
      return hexString.toString();
    } catch (Exception e) {
      log.error("Error generating signature: ", e);
      return "";
    }
  }
}