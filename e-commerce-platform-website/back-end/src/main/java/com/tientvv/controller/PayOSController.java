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
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.tientvv.model.Order;
import org.springframework.http.HttpStatus;
import com.tientvv.repository.TransactionRepository;
import com.tientvv.model.Transaction;
import com.tientvv.dto.order.OrderDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Collectors;
import com.tientvv.repository.OrderItemRepository;
import com.tientvv.model.OrderItem;
import com.tientvv.service.EmailService;

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
    private final OrderItemRepository orderItemRepository;
    private final EmailService emailService;


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

            // Kiểm tra xem có phải nhiều đơn hàng không
            Boolean multipleOrders = null;
            Integer totalOrders = null;
            String pendingOrdersJson = null;
            
            if (orderData.getAdditionalData() != null) {
                multipleOrders = (Boolean) orderData.getAdditionalData().get("multipleOrders");
                totalOrders = (Integer) orderData.getAdditionalData().get("totalOrders");
                pendingOrdersJson = (String) orderData.getAdditionalData().get("pendingOrders");
            }
            
            log.info("Multiple orders flag: {}, Total orders: {}", multipleOrders, totalOrders);

            OrderDto createdOrder;
            if (Boolean.TRUE.equals(multipleOrders) && totalOrders != null && totalOrders > 1) {
                // Tạo tất cả đơn hàng cho nhiều shop
                log.info("Creating multiple orders for {} shops", totalOrders);
                
                // Lấy danh sách đơn hàng từ additionalData
                if (pendingOrdersJson != null) {
                    // Parse và tạo tất cả đơn hàng
                    List<CreateOrderDto> allOrdersData = parseOrdersFromJson(pendingOrdersJson);
                    List<OrderDto> createdOrders = new ArrayList<>();
                    
                    log.info("Parsed {} orders from JSON", allOrdersData.size());
                    
                    for (int i = 0; i < allOrdersData.size(); i++) {
                        CreateOrderDto orderDto = allOrdersData.get(i);
                        try {
                            log.info("Creating order {}/{} for shop {} with {} items", 
                                i + 1, allOrdersData.size(), orderDto.getShopId(), 
                                orderDto.getOrderItems() != null ? orderDto.getOrderItems().size() : 0);
                            
                            OrderDto order = orderService.createOrder(orderDto);
                            createdOrders.add(order);
                            log.info("Created order for shop {}: {} with {} items", 
                                orderDto.getShopId(), order.getId(), 
                                order.getOrderItems() != null ? order.getOrderItems().size() : 0);
                        } catch (Exception e) {
                            log.error("Failed to create order for shop {}: {}", orderDto.getShopId(), e.getMessage());
                            // Xóa các đơn hàng đã tạo nếu có lỗi
                            for (OrderDto createdOrderToDelete : createdOrders) {
                                try {
                                    orderService.deleteOrder(createdOrderToDelete.getId());
                                } catch (Exception deleteError) {
                                    log.error("Error deleting order {}: {}", createdOrderToDelete.getId(), deleteError.getMessage());
                                }
                            }
                            throw new RuntimeException("Lỗi tạo đơn hàng cho shop " + orderDto.getShopId() + ": " + e.getMessage());
                        }
                    }
                    
                    // Sử dụng đơn hàng đầu tiên làm đơn hàng chính cho PayOS
                    createdOrder = createdOrders.get(0);
                    log.info("Created {} orders successfully, using first order {} for PayOS", createdOrders.size(), createdOrder.getId());
                } else {
                    // Fallback: tạo đơn hàng hiện tại
                    log.warn("Multiple orders flag is true but no pending orders data found, creating single order");
                    createdOrder = orderService.createOrder(orderData);
                }
            } else {
                // Tạo đơn hàng đơn lẻ
                log.info("Creating single order");
                createdOrder = orderService.createOrder(orderData);
            }

            // Sử dụng orderCode từ đơn hàng đã tạo
            String orderCode = createdOrder.getOrderCode();
            log.info("Using orderCode from created order: {}", orderCode);

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
                response.put("orderId", createdOrder.getId().toString());
                response.put("message", "Tạo URL thanh toán PayOS thành công");

                log.info("PayOS payment URL created successfully: {}", result.get("paymentUrl"));
                return ResponseEntity.ok(response);
            } else {
                // Nếu có lỗi PayOS, xóa đơn hàng đã tạo
                log.error("PayOS error, deleting created order: {}", createdOrder.getId());
                try {
                    orderService.deleteOrder(createdOrder.getId());
                } catch (Exception deleteError) {
                    log.error("Error deleting order after PayOS failure: {}", deleteError.getMessage());
                }

                // Nếu có lỗi PayOS
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", result.get("message"));
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
    public ResponseEntity<Map<String, Object>> verifyPayment(@RequestBody Map<String, String> request) {
        try {
            String orderCode = request.get("orderCode");
            String transactionCode = request.get("transactionCode");
            
            log.info("Verifying PayOS payment - orderCode: {}, transactionCode: {}", orderCode, transactionCode);
            
            if (orderCode == null || transactionCode == null) {
                log.error("Missing required parameters - orderCode: {}, transactionCode: {}", orderCode, transactionCode);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Thiếu thông tin orderCode hoặc transactionCode");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Verify payment with PayOS
            boolean isPaymentValid = payOSService.verifyPayment(orderCode, transactionCode);
            log.info("PayOS verification result for orderCode {}: {}", orderCode, isPaymentValid);
            
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
                    String orderHashCode = String.valueOf(Math.abs(searchOrder.getId().hashCode()));
                    String orderCodeFromDB = searchOrder.getOrderCode();
                    
                    log.info("Checking order {}: UUID={}, hashCode={}, orderCode={}", 
                            searchOrder.getId(), orderIdString, orderHashCode, orderCodeFromDB);
                    
                    // So sánh với orderCode từ PayOS (có thể là UUID, hashCode, hoặc orderCode từ DB)
                    if (orderCode.equals(orderIdString) || 
                        orderCode.equals(orderHashCode) ||
                        orderCode.equals(orderCodeFromDB) ||
                        orderCode.equals(orderIdString.replace("-", "")) ||
                        orderCode.equals(orderIdString.substring(0, 8))) {
                        
                        order = searchOrder;
                        log.info("Found matching order: {} (UUID: {}, hashCode: {}, orderCode: {})", 
                                orderCode, orderIdString, orderHashCode, orderCodeFromDB);
                        break;
                    }
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
                // Cập nhật trạng thái transaction thành SUCCESS và order status thành PROCESSED
                var updatedOrder = orderService.updateTransactionStatus(order.getId(), "SUCCESS");
                
                log.info("Updated transaction status to SUCCESS and order status to PROCESSED for order {}", order.getId());
                
                // Kiểm tra xem có nhiều đơn hàng liên quan không
                // Lưu thông tin nhiều đơn hàng vào localStorage thay vì Order entity
                log.info("Payment success for order {}, checking for related orders", order.getId());
                
                // Lưu accountId để tránh lỗi effectively final
                final UUID accountId = order.getAccount().getId();
                
                // Sử dụng thời gian tạo đơn hàng để xác định đơn hàng cùng lần thanh toán
                OffsetDateTime orderTime = order.getOrderDate();
                OffsetDateTime timeWindow = orderTime.plusMinutes(5); // Cửa sổ 5 phút
                
                // Tìm tất cả đơn hàng PENDING_PROCESSING và lọc theo account và thời gian
                List<Order> allPendingOrders = orderRepository.findByOrderStatus("PENDING_PROCESSING");
                List<Order> pendingOrders = allPendingOrders.stream()
                    .filter(o -> o.getAccount().getId().equals(accountId))
                    .filter(o -> {
                        // Chỉ lấy đơn hàng trong cửa sổ thời gian 5 phút từ đơn hàng hiện tại
                        OffsetDateTime orderDate = o.getOrderDate();
                        return orderDate != null && 
                               !orderDate.isBefore(orderTime.minusMinutes(5)) && 
                               !orderDate.isAfter(timeWindow);
                    })
                    .collect(Collectors.toList());
                
                log.info("Found {} total PENDING_PROCESSING orders, {} for account {} in time window ({} to {})", 
                    allPendingOrders.size(), pendingOrders.size(), accountId, 
                    orderTime.minusMinutes(5), timeWindow);
                
                if (pendingOrders.size() > 1) {
                    log.info("Found {} pending orders for account {} in time window, updating all", 
                        pendingOrders.size(), accountId);
                    
                    // Cập nhật tất cả đơn hàng PENDING_PROCESSING trong cửa sổ thời gian
                    for (Order pendingOrder : pendingOrders) {
                        if (!pendingOrder.getId().equals(order.getId())) {
                            try {
                                log.info("Updating related order {} for shop {} (orderDate: {})", 
                                    pendingOrder.getId(), pendingOrder.getShop().getId(), pendingOrder.getOrderDate());
                                orderService.updateTransactionStatus(pendingOrder.getId(), "SUCCESS");
                            } catch (Exception e) {
                                log.error("Error updating related order {}: {}", pendingOrder.getId(), e.getMessage());
                            }
                        }
                    }
                }
                
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
                response.put("orderStatus", "PROCESSED"); // Updated order status
                response.put("transactionStatus", "SUCCESS"); // Set transaction status
                response.put("transactionCount", transactions.size());
            } else if (shouldMarkAsCancelled) {
                // Kiểm tra xem có nhiều đơn hàng liên quan không
                final UUID accountId = order.getAccount().getId();
                
                // Thay vì tìm tất cả đơn hàng PENDING_PROCESSING, chỉ tìm đơn hàng thực sự liên quan
                // Sử dụng thời gian tạo đơn hàng để xác định đơn hàng cùng lần thanh toán
                OffsetDateTime orderTime = order.getOrderDate();
                OffsetDateTime timeWindow = orderTime.plusMinutes(5); // Cửa sổ 5 phút
                
                List<Order> allPendingOrders = orderRepository.findByOrderStatus("PENDING_PROCESSING");
                List<Order> pendingOrders = allPendingOrders.stream()
                    .filter(o -> o.getAccount().getId().equals(accountId))
                    .filter(o -> {
                        // Chỉ lấy đơn hàng trong cửa sổ thời gian 5 phút từ đơn hàng hiện tại
                        OffsetDateTime orderDate = o.getOrderDate();
                        return orderDate != null && 
                               !orderDate.isBefore(orderTime.minusMinutes(5)) && 
                               !orderDate.isAfter(timeWindow);
                    })
                    .collect(Collectors.toList());
                
                log.info("Found {} total PENDING_PROCESSING orders, {} for account {} in time window ({} to {})", 
                    allPendingOrders.size(), pendingOrders.size(), accountId, 
                    orderTime.minusMinutes(5), timeWindow);
                
                // Log chi tiết từng đơn hàng
                for (Order pendingOrder : pendingOrders) {
                    log.info("Pending order {}: status={}, shopId={}, totalAmount={}, orderDate={}", 
                        pendingOrder.getId(), pendingOrder.getOrderStatus(), 
                        pendingOrder.getShop().getId(), pendingOrder.getTotalAmount(), 
                        pendingOrder.getOrderDate());
                }
                
                boolean hasMultipleOrders = pendingOrders.size() > 1;
                
                // Cập nhật trạng thái transaction thành CANCELLED và order status thành CANCELLED
                // Chỉ gửi email từ updateTransactionStatus nếu không có nhiều đơn hàng
                var updatedOrder = orderService.updateTransactionStatus(order.getId(), "CANCELLED", !hasMultipleOrders);
                orderService.updateOrderStatus(order.getId(), "CANCELLED", false);
                
                log.info("Updated transaction status to CANCELLED and order status to CANCELLED for order {}", order.getId());
                
                if (hasMultipleOrders) {
                    log.info("Found {} pending orders for account {} in time window, sending multiple orders cancellation email", 
                        pendingOrders.size(), accountId);
                    
                    // Gửi email tổng hợp cho tất cả đơn hàng
                    try {
                        List<OrderDto> allOrderDtos = new ArrayList<>();
                        
                        // Chỉ thêm đơn hàng trong cửa sổ thời gian
                        for (Order pendingOrder : pendingOrders) {
                            // Chỉ thêm đơn hàng nếu nó chưa bị hủy hoặc là đơn hàng hiện tại
                            if (!"CANCELLED".equals(pendingOrder.getOrderStatus()) || 
                                pendingOrder.getId().equals(order.getId())) {
                                OrderDto orderDto = orderService.getOrderById(pendingOrder.getId());
                                allOrderDtos.add(orderDto);
                                log.info("Added order {} to cancellation email (status: {}, orderDate: {})", 
                                    pendingOrder.getId(), pendingOrder.getOrderStatus(), pendingOrder.getOrderDate());
                            } else {
                                log.info("Skipped order {} as it's already cancelled", pendingOrder.getId());
                            }
                        }
                        
                        // Gửi email tổng hợp thay vì email riêng lẻ
                        emailService.sendMultipleOrdersCancellationEmail(allOrderDtos);
                        log.info("Multiple orders cancellation email sent successfully for {} orders", allOrderDtos.size());
                    } catch (Exception e) {
                        log.error("Error sending multiple orders cancellation email: {}", e.getMessage());
                    }
                }
                
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
                    String orderHashCode = String.valueOf(Math.abs(searchOrder.getId().hashCode()));
                    String orderCodeFromDB = searchOrder.getOrderCode();
                    
                    log.info("Checking order {}: UUID={}, hashCode={}, orderCode={}", 
                            searchOrder.getId(), orderIdString, orderHashCode, orderCodeFromDB);
                    
                    // So sánh với orderCode từ PayOS (có thể là UUID, hashCode, hoặc orderCode từ DB)
                    if (orderCode.equals(orderIdString) || 
                        orderCode.equals(orderHashCode) ||
                        orderCode.equals(orderCodeFromDB) ||
                        orderCode.equals(orderIdString.replace("-", "")) ||
                        orderCode.equals(orderIdString.substring(0, 8))) {
                        
                        order = searchOrder;
                        log.info("Found matching order: {} (UUID: {}, hashCode: {}, orderCode: {})", 
                                orderCode, orderIdString, orderHashCode, orderCodeFromDB);
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
                    // Cập nhật transaction status thành SUCCESS và order status thành PROCESSED
                    var updatedOrder = orderService.updateTransactionStatus(order.getId(), "SUCCESS");
                    log.info("Updated transaction status to SUCCESS and order status to PROCESSED for order {} based on PayOS callback", order.getId());
                    
                    return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Transaction and order status updated successfully",
                        "orderId", order.getId(),
                        "orderStatus", "PROCESSED",
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
                    String orderHashCode = String.valueOf(Math.abs(searchOrder.getId().hashCode()));
                    String orderCodeFromDB = searchOrder.getOrderCode();
                    
                    log.info("Checking order {}: UUID={}, hashCode={}, orderCode={}", 
                            searchOrder.getId(), orderIdString, orderHashCode, orderCodeFromDB);
                    
                    // So sánh với orderCode từ PayOS (có thể là UUID, hashCode, hoặc orderCode từ DB)
                    if (orderCode.equals(orderIdString) || 
                        orderCode.equals(orderHashCode) ||
                        orderCode.equals(orderCodeFromDB) ||
                        orderCode.equals(orderIdString.replace("-", "")) ||
                        orderCode.equals(orderIdString.substring(0, 8))) {
                        
                        order = searchOrder;
                        log.info("Found matching order: {} (UUID: {}, hashCode: {}, orderCode: {})", 
                                orderCode, orderIdString, orderHashCode, orderCodeFromDB);
                        break;
                    }
                }
            }
            
            if (order == null) {
                log.error("Order not found for cancel callback - orderCode: {}", orderCode);
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Order not found"));
            }
            
            // Cập nhật đơn hàng thành CANCELLED khi user nhấn hủy trên PayOS
            // Chỉ gửi email từ updateTransactionStatus, không gửi từ updateOrderStatus
            var updatedOrder = orderService.updateTransactionStatus(order.getId(), "CANCELLED", true);
            orderService.updateOrderStatus(order.getId(), "CANCELLED", false);
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

    private List<CreateOrderDto> parseOrdersFromJson(String jsonString) {
        List<CreateOrderDto> orders = new ArrayList<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            orders = objectMapper.readValue(jsonString, new TypeReference<List<CreateOrderDto>>() {});
        } catch (Exception e) {
            log.error("Error parsing JSON string to CreateOrderDto list: {}", e.getMessage());
            throw new RuntimeException("Lỗi phân tích dữ liệu đơn hàng từ JSON: " + e.getMessage());
        }
        return orders;
    }
} 