package com.tientvv.controller;

import com.tientvv.dto.order.CreateOrderDto;
import com.tientvv.repository.OrderRepository;
import com.tientvv.service.OrderService;
import com.tientvv.service.PayOSService;
import com.tientvv.service.PendingOrderService;
import com.tientvv.service.PaymentExpiryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.tientvv.model.Order;
import org.springframework.http.HttpStatus;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/api/payos")
@RequiredArgsConstructor
@Slf4j
public class PayOSController {

    private final PayOSService payOSService;
    private final PendingOrderService pendingOrderService;
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final PaymentExpiryService paymentExpiryService;


    @PostMapping("/create-payment")
    public ResponseEntity<Map<String, Object>> createPayment(@RequestBody CreateOrderDto orderData) {
        try {
            log.info("Creating PayOS payment for order data: {}", orderData);
            log.info("Total amount received: {}", orderData.getTotalAmount());
            log.info("Account ID: {}", orderData.getAccountId());
            log.info("Order items count: {}", orderData.getOrderItems() != null ? orderData.getOrderItems().size() : 0);

            // Validate required fields
            if (orderData.getAccountId() == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Account ID không được để trống");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (orderData.getTotalAmount() == null || orderData.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Tổng tiền phải lớn hơn 0");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Tạo đơn hàng thật ngay lập tức với trạng thái PENDING
            var order = orderService.createOrder(orderData);
            String orderCode = order.getId().toString();

            // Tính tổng tiền (PayOS yêu cầu số tiền là VND, không có phần thập phân)
            Long amount = orderData.getTotalAmount().longValue();

            // Tạo description đơn giản để tránh lỗi encoding
            String description = "Payment for order " + orderCode;

            // Tạo PayOS payment URL
            Map<String, Object> result = payOSService.createPaymentUrl(orderCode, amount, description);

            if ((Boolean) result.get("success")) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("paymentUrl", result.get("paymentUrl"));
                response.put("orderCode", result.get("orderCode"));
                response.put("amount", result.get("amount"));
                response.put("transactionCode", result.get("transactionCode"));
                response.put("message", "Tạo URL thanh toán PayOS thành công");

                log.info("PayOS payment URL created successfully: {}", result.get("paymentUrl"));
                return ResponseEntity.ok(response);
            } else {
                // Nếu có lỗi PayOS, đơn hàng vẫn được tạo với trạng thái PENDING
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", result.get("message"));
                errorResponse.put("orderId", order.getId());
                errorResponse.put("orderCode", orderCode);
                if (result.containsKey("errorCode")) {
                    errorResponse.put("errorCode", result.get("errorCode"));
                }
                return ResponseEntity.badRequest().body(errorResponse);
            }

        } catch (Exception e) {
            log.error("Error creating PayOS payment: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi tạo thanh toán: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<Map<String, Object>> verifyPayment(@RequestBody Map<String, Object> request) {
        try {
            String transactionCode = (String) request.get("transactionCode");
            String orderCode = (String) request.get("orderCode");

            if (transactionCode == null || orderCode == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "TransactionCode và orderCode là bắt buộc");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Verify payment với PayOS
            boolean isValid = payOSService.verifyPayment(orderCode, transactionCode);

            Map<String, Object> response = new HashMap<>();
            if (isValid) {
                // Tìm đơn hàng theo orderCode
                Order order = null;
                
                // Thử tìm theo UUID trước
                try {
                    UUID orderId = UUID.fromString(orderCode);
                    order = orderRepository.findById(orderId).orElse(null);
                    if (order != null) {
                        log.info("Found order by UUID: {}", order.getId());
                    }
                } catch (IllegalArgumentException e) {
                    log.info("orderCode {} is not a valid UUID, searching by orderCode", orderCode);
                }
                
                // Nếu không tìm thấy theo UUID, tìm theo orderCode trong tất cả đơn hàng
                if (order == null) {
                    List<Order> allOrders = orderRepository.findAll();
                    log.info("Searching through {} orders for orderCode: {}", allOrders.size(), orderCode);
                    
                    for (Order searchOrder : allOrders) {
                        String orderIdString = searchOrder.getId().toString();
                        String orderHashCode = String.valueOf(searchOrder.getId().hashCode());
                        
                        // So sánh với orderCode từ PayOS (có thể là UUID, hashCode, hoặc số)
                        if (orderCode.equals(orderIdString) || 
                            orderCode.equals(orderHashCode) ||
                            orderCode.equals(orderIdString.replace("-", "")) ||
                            orderCode.equals(orderIdString.substring(0, 8))) {
                            
                            order = searchOrder;
                            log.info("Found matching order: {} (UUID: {}, hashCode: {})", 
                                    orderCode, orderIdString, orderHashCode);
                            break;
                        }
                    }
                }
                
                if (order != null) {
                    // Cập nhật trạng thái đơn hàng thành PAID
                    var updatedOrder = orderService.updateOrderStatus(order.getId(), "PAID");
                    
                    response.put("success", true);
                    response.put("message", "Payment verified successfully");
                    response.put("orderId", order.getId().toString());
                    response.put("orderCode", orderCode);
                    response.put("transactionCode", transactionCode);
                } else {
                    response.put("success", false);
                    response.put("message", "Order not found");
                }
            } else {
                response.put("success", false);
                response.put("message", "Payment verification failed");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error verifying PayOS payment: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi xác minh thanh toán: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/callback")
    public ResponseEntity<Map<String, Object>> handleCallback(@RequestBody Map<String, Object> callbackData) {
        try {
            log.info("Received PayOS callback: {}", callbackData);
            
            // Extract data from callback
            String orderCode = (String) callbackData.get("orderCode");
            String status = (String) callbackData.get("status");
            String transactionCode = (String) callbackData.get("transactionCode");
            
            log.info("Callback - orderCode: {}, status: {}, transactionCode: {}", orderCode, status, transactionCode);
            
            if (orderCode == null || status == null) {
                log.error("Invalid callback data - missing orderCode or status");
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid callback data"));
            }
            
            // Tìm đơn hàng theo orderCode
            Order order = null;
            
            // Thử tìm theo UUID trước
            try {
                UUID orderId = UUID.fromString(orderCode);
                order = orderRepository.findById(orderId).orElse(null);
                if (order != null) {
                    log.info("Found order by UUID: {}", order.getId());
                }
            } catch (IllegalArgumentException e) {
                log.info("orderCode {} is not a valid UUID, searching by orderCode", orderCode);
            }
            
            // Nếu không tìm thấy theo UUID, tìm theo orderCode trong tất cả đơn hàng
            if (order == null) {
                List<Order> allOrders = orderRepository.findAll();
                log.info("Searching through {} orders for orderCode: {}", allOrders.size(), orderCode);
                
                for (Order searchOrder : allOrders) {
                    String orderIdString = searchOrder.getId().toString();
                    String orderHashCode = String.valueOf(searchOrder.getId().hashCode());
                    
                    // So sánh với orderCode từ PayOS (có thể là UUID, hashCode, hoặc số)
                    if (orderCode.equals(orderIdString) || 
                        orderCode.equals(orderHashCode) ||
                        orderCode.equals(orderIdString.replace("-", "")) ||
                        orderCode.equals(orderIdString.substring(0, 8))) {
                        
                        order = searchOrder;
                        log.info("Found matching order: {} (UUID: {}, hashCode: {})", 
                                orderCode, orderIdString, orderHashCode);
                        break;
                    }
                }
            }
            
            if (order == null) {
                log.error("Order not found for callback - orderCode: {}", orderCode);
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Order not found"));
            }
            
            // Update order status based on PayOS status
            String newStatus = null;
            switch (status) {
                case "PAID":
                    newStatus = "PAID";
                    break;
                case "EXPIRED":
                case "CANCELLED":
                    newStatus = "CANCELLED";
                    break;
                default:
                    log.warn("Unknown PayOS status: {}", status);
                    return ResponseEntity.ok(Map.of("success", true, "message", "Unknown status ignored"));
            }
            
            if (newStatus != null) {
                var updatedOrder = orderService.updateOrderStatus(order.getId(), newStatus);
                log.info("Updated order {} from {} to {} based on PayOS callback", 
                        order.getId(), order.getOrderStatus(), newStatus);
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Order status updated successfully",
                    "orderId", order.getId(),
                    "oldStatus", order.getOrderStatus(),
                    "newStatus", newStatus,
                    "payosStatus", status
                ));
            }
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Callback processed"));
            
        } catch (Exception e) {
            log.error("Error processing PayOS callback: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error processing callback"));
        }
    }

    @PostMapping("/cancel-callback")
    public ResponseEntity<Map<String, Object>> handleCancelCallback(@RequestBody Map<String, Object> cancelData) {
        try {
            log.info("Received PayOS cancel callback: {}", cancelData);
            
            // Extract data from cancel callback
            String orderCode = (String) cancelData.get("orderCode");
            String status = (String) cancelData.get("status");
            String transactionCode = (String) cancelData.get("transactionCode");
            
            log.info("Cancel callback - orderCode: {}, status: {}, transactionCode: {}", orderCode, status, transactionCode);
            
            if (orderCode == null) {
                log.error("Invalid cancel callback data - missing orderCode");
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid cancel callback data"));
            }
            
            // Tìm đơn hàng theo orderCode
            Order order = null;
            
            // Thử tìm theo UUID trước
            try {
                UUID orderId = UUID.fromString(orderCode);
                order = orderRepository.findById(orderId).orElse(null);
                if (order != null) {
                    log.info("Found order by UUID: {}", order.getId());
                }
            } catch (IllegalArgumentException e) {
                log.info("orderCode {} is not a valid UUID, searching by orderCode", orderCode);
            }
            
            // Nếu không tìm thấy theo UUID, tìm theo orderCode trong tất cả đơn hàng
            if (order == null) {
                List<Order> allOrders = orderRepository.findAll();
                log.info("Searching through {} orders for orderCode: {}", allOrders.size(), orderCode);
                
                for (Order searchOrder : allOrders) {
                    String orderIdString = searchOrder.getId().toString();
                    String orderHashCode = String.valueOf(searchOrder.getId().hashCode());
                    
                    // So sánh với orderCode từ PayOS (có thể là UUID, hashCode, hoặc số)
                    if (orderCode.equals(orderIdString) || 
                        orderCode.equals(orderHashCode) ||
                        orderCode.equals(orderIdString.replace("-", "")) ||
                        orderCode.equals(orderIdString.substring(0, 8))) {
                        
                        order = searchOrder;
                        log.info("Found matching order: {} (UUID: {}, hashCode: {})", 
                                orderCode, orderIdString, orderHashCode);
                        break;
                    }
                }
            }
            
            if (order == null) {
                log.error("Order not found for cancel callback - orderCode: {}", orderCode);
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Order not found"));
            }
            
            // Cập nhật đơn hàng thành CANCELLED khi user nhấn hủy trên PayOS
            var updatedOrder = orderService.updateOrderStatus(order.getId(), "CANCELLED");
            log.info("Cancelled order {} from {} to CANCELLED based on PayOS cancel callback", 
                    order.getId(), order.getOrderStatus());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Order cancelled successfully",
                "orderId", order.getId(),
                "oldStatus", order.getOrderStatus(),
                "newStatus", "CANCELLED",
                "payosStatus", status
            ));
            
        } catch (Exception e) {
            log.error("Error processing PayOS cancel callback: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error processing cancel callback"));
        }
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        try {
            Map<String, Object> config = payOSService.getConfig();
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            log.error("Error getting PayOS config: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi lấy cấu hình PayOS: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/cancel-current-order")
    public ResponseEntity<Map<String, Object>> cancelCurrentOrder(@RequestParam String orderCode) {
        try {
            log.info("Cancelling current order: {}", orderCode);
            
            // Tìm đơn hàng theo orderCode
            Order order = null;
            
            // Thử tìm theo UUID trước
            try {
                UUID orderId = UUID.fromString(orderCode);
                order = orderRepository.findById(orderId).orElse(null);
                if (order != null) {
                    log.info("Found order by UUID: {}", order.getId());
                }
            } catch (IllegalArgumentException e) {
                log.info("orderCode {} is not a valid UUID, searching by orderCode", orderCode);
            }
            
            // Nếu không tìm thấy theo UUID, tìm theo orderCode trong tất cả đơn hàng
            if (order == null) {
                List<Order> allOrders = orderRepository.findAll();
                log.info("Searching through {} orders for orderCode: {}", allOrders.size(), orderCode);
                
                for (Order searchOrder : allOrders) {
                    String orderIdString = searchOrder.getId().toString();
                    String orderHashCode = String.valueOf(searchOrder.getId().hashCode());
                    
                    // So sánh với orderCode từ PayOS (có thể là UUID, hashCode, hoặc số)
                    if (orderCode.equals(orderIdString) || 
                        orderCode.equals(orderHashCode) ||
                        orderCode.equals(orderIdString.replace("-", "")) ||
                        orderCode.equals(orderIdString.substring(0, 8))) {
                        
                        order = searchOrder;
                        log.info("Found matching order: {} (UUID: {}, hashCode: {})", 
                                orderCode, orderIdString, orderHashCode);
                        break;
                    }
                }
            }
            
            // Nếu không tìm thấy theo orderCode, tìm đơn hàng PENDING_PAYMENT gần nhất
            if (order == null) {
                log.info("🔍 OrderCode '{}' not found, searching for most recent PENDING_PAYMENT order", orderCode);
                
                List<Order> pendingOrders = orderRepository.findByOrderStatus("PENDING_PAYMENT");
                if (!pendingOrders.isEmpty()) {
                    // Lấy đơn hàng PENDING_PAYMENT gần nhất
                    order = pendingOrders.get(0);
                    log.info("✅ Found most recent PENDING_PAYMENT order: {} (UUID: {}, hashCode: {})", 
                            order.getId(), order.getId().toString(), order.getId().hashCode());
                } else {
                    // Thử tìm đơn hàng PENDING
                    List<Order> pendingBasicOrders = orderRepository.findByOrderStatus("PENDING");
                    if (!pendingBasicOrders.isEmpty()) {
                        order = pendingBasicOrders.get(0);
                        log.info("✅ Found most recent PENDING order: {} (UUID: {}, hashCode: {})", 
                                order.getId(), order.getId().toString(), order.getId().hashCode());
                    }
                }
            }
            
            if (order == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Không tìm thấy đơn hàng chờ thanh toán");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            if (!"PENDING_PAYMENT".equals(order.getOrderStatus()) && !"PENDING".equals(order.getOrderStatus())) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Đơn hàng không ở trạng thái chờ thanh toán");
                response.put("currentStatus", order.getOrderStatus());
                return ResponseEntity.badRequest().body(response);
            }
            
            // Cancel đơn hàng hiện tại
            orderService.updateOrderStatus(order.getId(), "CANCELLED");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã hủy đơn hàng thành công");
            response.put("orderId", order.getId());
            response.put("orderCode", orderCode);
            response.put("oldStatus", order.getOrderStatus());
            response.put("newStatus", "CANCELLED");
            response.put("totalAmount", order.getTotalAmount());
            
            log.info("Successfully cancelled order {} from {} to CANCELLED", 
                    order.getId(), order.getOrderStatus());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error cancelling current order: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi hủy đơn hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/test-expire-orders")
    public ResponseEntity<Map<String, Object>> testExpireOrders() {
        try {
            log.info("🧪 Testing expired orders cancellation...");
            
            // Gọi service để hủy đơn hàng hết hạn
            paymentExpiryService.cancelExpiredOrders();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã kiểm tra và hủy đơn hàng hết hạn");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error testing expired orders: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi test hủy đơn hàng hết hạn: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

} 