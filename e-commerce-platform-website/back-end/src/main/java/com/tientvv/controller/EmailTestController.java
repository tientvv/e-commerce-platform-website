package com.tientvv.controller;

import com.tientvv.dto.order.OrderDto;
import com.tientvv.service.EmailService;
import com.tientvv.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/test/email")
public class EmailTestController {

    @Autowired
    private EmailService emailService;

    @SuppressWarnings("unused")
    @Autowired
    private OrderService orderService;

    @PostMapping("/test-order-confirmation")
    public ResponseEntity<Map<String, Object>> testOrderConfirmationEmail(
            @RequestParam String email) {
        try {
            // Create a test order DTO
            OrderDto testOrder = createTestOrder(email);
            
            // Send test email
            emailService.sendOrderConfirmationEmail(testOrder);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Test order confirmation email sent to: " + email);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error sending test email: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/test-status-update")
    public ResponseEntity<Map<String, Object>> testStatusUpdateEmail(
            @RequestParam String email,
            @RequestParam String oldStatus,
            @RequestParam String newStatus) {
        try {
            // Create a test order DTO
            OrderDto testOrder = createTestOrder(email);
            
            // Send test email
            emailService.sendOrderStatusUpdateEmail(testOrder, oldStatus, newStatus);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Test status update email sent to: " + email);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error sending test email: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/test-cancellation")
    public ResponseEntity<Map<String, Object>> testCancellationEmail(
            @RequestParam String email) {
        try {
            // Create a test order DTO
            OrderDto testOrder = createTestOrder(email);
            testOrder.setCancelledDate(OffsetDateTime.now());
            
            // Send test email
            emailService.sendOrderCancellationEmail(testOrder);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Test cancellation email sent to: " + email);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error sending test email: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/test-delivery")
    public ResponseEntity<Map<String, Object>> testDeliveryEmail(
            @RequestParam String email) {
        try {
            // Create a test order DTO
            OrderDto testOrder = createTestOrder(email);
            testOrder.setDeliveredDate(OffsetDateTime.now());
            
            // Send test email
            emailService.sendOrderDeliveryEmail(testOrder);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Test delivery email sent to: " + email);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error sending test email: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    private OrderDto createTestOrder(String email) {
        OrderDto order = new OrderDto();
        order.setId(UUID.randomUUID());
        order.setOrderCode("TEST123456");
        order.setOrderDate(OffsetDateTime.now());
        order.setAccountName("Nguyễn Văn Test");
        order.setAccountEmail(email);
        order.setAccountPhone("0123456789");
        order.setShopName("Shop Test");
        order.setShippingMethod("Giao hàng tiêu chuẩn");
        order.setShippingPrice(new BigDecimal("30000"));
        order.setPaymentName("Thanh toán khi nhận hàng");
        order.setOrderStatus("PENDING_PROCESSING");
        order.setTotalAmount(new BigDecimal("150000"));
        order.setDiscountAmount(new BigDecimal("10000"));
        order.setShippingAddress("123 Đường Test, Quận 1, TP.HCM");

        // Create test order items
        List<OrderDto.OrderItemDto> orderItems = new ArrayList<>();
        
        OrderDto.OrderItemDto item1 = new OrderDto.OrderItemDto();
        item1.setProductName("Sản phẩm Test 1");
        item1.setQuantity(2);
        item1.setProductPrice(new BigDecimal("50000"));
        orderItems.add(item1);
        
        OrderDto.OrderItemDto item2 = new OrderDto.OrderItemDto();
        item2.setProductName("Sản phẩm Test 2");
        item2.setQuantity(1);
        item2.setProductPrice(new BigDecimal("80000"));
        orderItems.add(item2);
        
        order.setOrderItems(orderItems);
        
        return order;
    }
}
