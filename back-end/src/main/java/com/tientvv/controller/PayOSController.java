package com.tientvv.controller;

import com.tientvv.dto.order.CreateOrderDto;
import com.tientvv.service.OrderService;
import com.tientvv.service.PayOSService;
import com.tientvv.service.PendingOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payos")
@RequiredArgsConstructor
@Slf4j
public class PayOSController {

  private final PayOSService payOSService;
  private final PendingOrderService pendingOrderService;
  private final OrderService orderService;

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

  @PostMapping("/create-payment")
  public ResponseEntity<Map<String, Object>> createPayment(@RequestBody CreateOrderDto orderData) {
    try {
      log.info("Creating PayOS payment for order data: {}", orderData);

      // Tạo đơn hàng tạm thời
      String orderCode = pendingOrderService.createPendingOrder(orderData);

      // Tính tổng tiền
      Long amount = orderData.getTotalAmount().longValue();

      // Tạo description
      String description = "Thanh toán đơn hàng " + orderCode;

      // Tạo PayOS payment URL
      Map<String, Object> result = payOSService.createPaymentUrl(orderCode, amount, description);

      if ((Boolean) result.get("success")) {
        // Thêm orderCode vào response
        result.put("orderCode", orderCode);
        return ResponseEntity.ok(result);
      } else {
        // Xóa đơn hàng tạm thời nếu tạo payment URL thất bại
        pendingOrderService.removePendingOrder(orderCode);
        return ResponseEntity.badRequest().body(result);
      }

    } catch (Exception e) {
      log.error("Error creating PayOS payment: ", e);
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("message", "Lỗi tạo thanh toán: " + e.getMessage());
      return ResponseEntity.badRequest().body(errorResponse);
    }
  }

  @PostMapping("/callback")
  public ResponseEntity<Map<String, Object>> paymentCallback(@RequestBody Map<String, Object> callbackData) {
    try {
      log.info("PayOS callback received: {}", callbackData);

      boolean isValid = payOSService.verifyPaymentCallback(callbackData);

      Map<String, Object> response = new HashMap<>();
      if (isValid) {
        // Lấy orderCode từ callback
        String orderCode = (String) callbackData.get("orderCode");

        // Tạo đơn hàng thật từ pending order
        try {
          PendingOrderService.PendingOrderData pendingOrder = pendingOrderService.getPendingOrder(orderCode);
          if (pendingOrder != null) {
            // Tạo đơn hàng thật
            var order = orderService.createOrder(pendingOrder.getOrderData());

            // Xóa pending order
            pendingOrderService.removePendingOrder(orderCode);

            response.put("success", true);
            response.put("message", "Payment verified and order created successfully");
            response.put("orderId", order.getId());
          } else {
            response.put("success", false);
            response.put("message", "Pending order not found");
          }
        } catch (Exception e) {
          log.error("Error creating real order from pending order: ", e);
          response.put("success", false);
          response.put("message", "Error creating order: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
      } else {
        response.put("success", false);
        response.put("message", "Invalid payment callback");
        return ResponseEntity.badRequest().body(response);
      }

    } catch (Exception e) {
      log.error("Error processing PayOS callback: ", e);
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("message", "Lỗi xử lý callback: " + e.getMessage());
      return ResponseEntity.badRequest().body(errorResponse);
    }
  }

  @PostMapping("/verify-payment")
  public ResponseEntity<Map<String, Object>> verifyPayment(@RequestBody Map<String, Object> request) {
    try {
      String orderCode = (String) request.get("orderCode");
      String transactionCode = (String) request.get("transactionCode");

      if (orderCode == null || transactionCode == null) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "OrderCode và transactionCode là bắt buộc");
        return ResponseEntity.badRequest().body(errorResponse);
      }

      // Verify payment với PayOS
      boolean isValid = payOSService.verifyPayment(orderCode, transactionCode);

      Map<String, Object> response = new HashMap<>();
      if (isValid) {
        // Lấy pending order
        PendingOrderService.PendingOrderData pendingOrder = pendingOrderService.getPendingOrder(orderCode);
        if (pendingOrder != null) {
          // Tạo đơn hàng thật
          var order = orderService.createOrder(pendingOrder.getOrderData());

          // Xóa pending order
          pendingOrderService.removePendingOrder(orderCode);

          response.put("success", true);
          response.put("message", "Payment verified and order created successfully");
          response.put("orderId", order.getId());
          response.put("orderCode", orderCode);
        } else {
          response.put("success", false);
          response.put("message", "Pending order not found");
        }
      } else {
        response.put("success", false);
        response.put("message", "Payment verification failed");
      }

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("Error verifying payment: ", e);
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("message", "Lỗi xác minh thanh toán: " + e.getMessage());
      return ResponseEntity.badRequest().body(errorResponse);
    }
  }

  @GetMapping("/test")
  public ResponseEntity<Map<String, Object>> testPayOS() {
    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("message", "PayOS service is working");
    return ResponseEntity.ok(response);
  }

  @SuppressWarnings("unused")
  @GetMapping("/test-sdk")
  public ResponseEntity<Map<String, Object>> testPayOSSDK() {
    try {
      log.info("Testing PayOS SDK initialization...");

      // Thử khởi tạo PayOS SDK
      vn.payos.PayOS payOS = new vn.payos.PayOS(clientId, apiKey, checksumKey);

      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("message", "PayOS SDK initialized successfully");
      result.put("clientId", clientId);
      result.put("apiKey", apiKey != null ? "***" + apiKey.substring(apiKey.length() - 4) : "null");
      result.put("checksumKey", checksumKey != null ? "***" + checksumKey.substring(checksumKey.length() - 4) : "null");
      result.put("returnUrl", returnUrl);
      result.put("cancelUrl", cancelUrl);

      log.info("PayOS SDK test successful");
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      log.error("PayOS SDK test failed: ", e);
      Map<String, Object> error = new HashMap<>();
      error.put("success", false);
      error.put("message", "PayOS SDK error: " + e.getMessage());
      error.put("errorType", e.getClass().getSimpleName());
      return ResponseEntity.badRequest().body(error);
    }
  }
}