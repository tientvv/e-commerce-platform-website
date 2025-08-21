package com.tientvv.controller;

import com.tientvv.dto.order.CreateOrderDto;
import com.tientvv.repository.OrderRepository;
import com.tientvv.service.OrderService;
import com.tientvv.service.PayOSService;

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
import com.tientvv.repository.TransactionRepository;
import com.tientvv.model.Transaction;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/api/payos")
@RequiredArgsConstructor
@Slf4j
public class PayOSController {

    private final PayOSService payOSService;
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final TransactionRepository transactionRepository;


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

            if (orderCode == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "orderCode là bắt buộc");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            log.info("Processing payment verification for orderCode: {}, transactionCode: {}", orderCode, transactionCode);

            // Tìm đơn hàng theo orderCode
            Order order = null;
            
            // Thử tìm theo UUID trước
            try {
                UUID orderId = UUID.fromString(orderCode);
                order = orderRepository.findById(orderId).orElse(null);
                if (order != null) {
                    log.info("Found order by UUID: {}", order.getId());
                } else {
                    log.info("No order found by UUID: {}", orderId);
                }
            } catch (IllegalArgumentException e) {
                log.info("orderCode {} is not a valid UUID, searching by orderCode", orderCode);
            }
            
            // Nếu không tìm thấy theo UUID, tìm theo orderCode trong database
            if (order == null) {
                // Tìm theo orderCode field trong database
                List<Order> ordersByCode = orderRepository.findByOrderCode(orderCode);
                if (!ordersByCode.isEmpty()) {
                    order = ordersByCode.get(0);
                    log.info("Found order by orderCode field: {} (UUID: {})", orderCode, order.getId());
                } else {
                    log.info("No order found by orderCode field: {}", orderCode);
                }
            }
            
            // Nếu vẫn không tìm thấy, tìm theo hashCode (fallback)
            if (order == null) {
                List<Order> allOrders = orderRepository.findAll();
                log.info("Searching through {} orders for orderCode: {}", allOrders.size(), orderCode);
                
                for (Order searchOrder : allOrders) {
                    String orderIdString = searchOrder.getId().toString();
                    String orderHashCode = String.valueOf(Math.abs(searchOrder.getId().hashCode()));
                    
                    log.info("Checking order {}: UUID={}, hashCode={}, orderCode={}, status={}", 
                            searchOrder.getId(), orderIdString, orderHashCode, searchOrder.getOrderCode(), searchOrder.getOrderStatus());
                    
                    // So sánh với orderCode từ PayOS
                    if (orderCode.equals(orderIdString) || 
                        orderCode.equals(orderHashCode) ||
                        orderCode.equals(searchOrder.getOrderCode()) ||
                        orderCode.equals(orderIdString.replace("-", "")) ||
                        orderCode.equals(orderIdString.substring(0, 8))) {
                        
                        order = searchOrder;
                        log.info("Found matching order: {} (UUID: {}, hashCode: {}, orderCode: {})", 
                                orderCode, orderIdString, orderHashCode, searchOrder.getOrderCode());
                        break;
                    }
                }
            }
            
            // Nếu vẫn không tìm thấy, lấy đơn hàng PENDING_PROCESSING gần nhất
            if (order == null) {
                log.info("Order not found for orderCode: {}, trying to find most recent PENDING_PROCESSING order", orderCode);
                List<Order> pendingOrders = orderRepository.findByOrderStatus("PENDING_PROCESSING");
                if (!pendingOrders.isEmpty()) {
                    order = pendingOrders.get(0);
                    log.info("Using most recent PENDING_PROCESSING order: {} (UUID: {}, orderCode: {})", 
                            order.getId(), order.getId(), order.getOrderCode());
                }
            }
            
            if (order == null) {
                log.error("Order not found for orderCode: {}", orderCode);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Order not found");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Nếu transactionCode là PAYOS_SUCCESS, luôn cập nhật transaction status thành SUCCESS
            // Nếu transactionCode là PAYOS_CANCELLED, cập nhật transaction status thành CANCELLED
            boolean shouldMarkAsPaid = "PAYOS_SUCCESS".equals(transactionCode);
            boolean shouldMarkAsCancelled = "PAYOS_CANCELLED".equals(transactionCode);

            log.info("Order {} current status: {}", order.getId(), order.getOrderStatus());
            log.info("shouldMarkAsPaid: {}, shouldMarkAsCancelled: {}, transactionCode: {}", shouldMarkAsPaid, shouldMarkAsCancelled, transactionCode);

            Map<String, Object> response = new HashMap<>();
            if (shouldMarkAsPaid) {
                // Cập nhật trạng thái transaction thành SUCCESS (không thay đổi order status)
                var updatedOrder = orderService.updateTransactionStatus(order.getId(), "SUCCESS");
                
                log.info("Updated transaction status to SUCCESS for order {}", order.getId());
                
                // Kiểm tra xem transaction có được cập nhật không
                List<Transaction> transactions = transactionRepository.findByOrderId(order.getId());
                log.info("Found {} transactions for order {}", transactions.size(), order.getId());
                
                for (Transaction transaction : transactions) {
                    log.info("Transaction {} status: {}", transaction.getId(), transaction.getTransactionStatus());
                }
                
                response.put("success", true);
                response.put("message", "Payment verified successfully");
                response.put("orderId", order.getId().toString());
                response.put("orderCode", orderCode);
                response.put("transactionCode", transactionCode);
                response.put("orderStatus", order.getOrderStatus()); // Keep original order status
                response.put("transactionStatus", "SUCCESS"); // Set transaction status
                response.put("transactionCount", transactions.size());
            } else if (shouldMarkAsCancelled) {
                // Cập nhật trạng thái transaction thành CANCELLED và order status thành CANCELLED
                var updatedOrder = orderService.updateTransactionStatus(order.getId(), "CANCELLED");
                orderService.updateOrderStatus(order.getId(), "CANCELLED");
                
                log.info("Updated transaction status to CANCELLED and order status to CANCELLED for order {}", order.getId());
                
                List<Transaction> transactions = transactionRepository.findByOrderId(order.getId());
                
                response.put("success", true);
                response.put("message", "Order cancelled successfully");
                response.put("orderId", order.getId().toString());
                response.put("orderCode", orderCode);
                response.put("transactionCode", transactionCode);
                response.put("orderStatus", "CANCELLED");
                response.put("transactionStatus", "CANCELLED");
                response.put("transactionCount", transactions.size());
            } else {
                // Nếu không phải PAYOS_SUCCESS hoặc PAYOS_CANCELLED, trả về thông tin hiện tại
                List<Transaction> transactions = transactionRepository.findByOrderId(order.getId());
                String currentTransactionStatus = transactions.isEmpty() ? "PENDING" : transactions.get(0).getTransactionStatus();
                
                response.put("success", true);
                response.put("message", "Payment status checked");
                response.put("orderId", order.getId().toString());
                response.put("orderCode", orderCode);
                response.put("transactionCode", transactionCode);
                response.put("orderStatus", order.getOrderStatus());
                response.put("transactionStatus", currentTransactionStatus);
                response.put("transactionCount", transactions.size());
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
                    // Chỉ cập nhật transaction status thành SUCCESS, không thay đổi order status
                    var updatedOrder = orderService.updateTransactionStatus(order.getId(), "SUCCESS");
                    log.info("Updated transaction status to SUCCESS for order {} based on PayOS callback", order.getId());
                    
                    return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Transaction status updated successfully",
                        "orderId", order.getId(),
                        "orderStatus", order.getOrderStatus(),
                        "transactionStatus", "SUCCESS",
                        "payosStatus", status
                    ));
                case "EXPIRED":
                case "CANCELLED":
                    // Cập nhật transaction status thành CANCELLED và order status thành CANCELLED
                    var cancelledOrder = orderService.updateTransactionStatus(order.getId(), "CANCELLED");
                    orderService.updateOrderStatus(order.getId(), "CANCELLED");
                    log.info("Updated transaction status to CANCELLED and order status to CANCELLED for order {} based on PayOS callback", order.getId());
                    
                    return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Transaction and order status updated to CANCELLED",
                        "orderId", order.getId(),
                        "orderStatus", "CANCELLED",
                        "transactionStatus", "CANCELLED",
                        "payosStatus", status
                    ));
                default:
                    log.warn("Unknown PayOS status: {}", status);
                    return ResponseEntity.ok(Map.of("success", true, "message", "Unknown status ignored"));
            }
            
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
            orderService.updateTransactionStatus(order.getId(), "CANCELLED");
            log.info("Cancelled order {} from {} to CANCELLED based on PayOS cancel callback", 
                    order.getId(), order.getOrderStatus());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Order and transaction cancelled successfully",
                "orderId", order.getId(),
                "oldStatus", order.getOrderStatus(),
                "newStatus", "CANCELLED",
                "transactionStatus", "CANCELLED",
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
            
            // Nếu không tìm thấy theo orderCode, tìm đơn hàng PENDING_PROCESSING gần nhất
            if (order == null) {
                log.info("🔍 OrderCode '{}' not found, searching for most recent PENDING_PROCESSING order", orderCode);
                
                List<Order> pendingOrders = orderRepository.findByOrderStatus("PENDING_PROCESSING");
                if (!pendingOrders.isEmpty()) {
                    // Lấy đơn hàng PENDING_PROCESSING gần nhất
                    order = pendingOrders.get(0);
                    log.info("✅ Found most recent PENDING_PROCESSING order: {} (UUID: {}, hashCode: {})", 
                            order.getId(), order.getId().toString(), order.getId().hashCode());
                }
            }
            
            if (order == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Không tìm thấy đơn hàng chờ thanh toán");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            if (!"PENDING_PROCESSING".equals(order.getOrderStatus())) {
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

    @GetMapping("/debug/find-order")
    public ResponseEntity<Map<String, Object>> debugFindOrder(@RequestParam String orderCode) {
        try {
            log.info("Debug: Finding order for orderCode: {}", orderCode);
            
            List<Order> allOrders = orderRepository.findAll();
            log.info("Total orders in database: {}", allOrders.size());
            
            for (Order searchOrder : allOrders) {
                String orderIdString = searchOrder.getId().toString();
                String orderHashCode = String.valueOf(Math.abs(searchOrder.getId().hashCode()));
                
                log.info("Order {}: UUID={}, hashCode={}, status={}", 
                        searchOrder.getId(), orderIdString, orderHashCode, searchOrder.getOrderStatus());
                
                if (orderCode.equals(orderIdString) || 
                    orderCode.equals(orderHashCode) ||
                    orderCode.equals(orderIdString.replace("-", "")) ||
                    orderCode.equals(orderIdString.substring(0, 8))) {
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("found", true);
                    response.put("orderId", searchOrder.getId().toString());
                    response.put("orderStatus", searchOrder.getOrderStatus());
                    response.put("orderHashCode", orderHashCode);
                    response.put("orderCode", orderCode);
                    return ResponseEntity.ok(response);
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("found", false);
            response.put("orderCode", orderCode);
            response.put("totalOrders", allOrders.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error debugging find order: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

} 