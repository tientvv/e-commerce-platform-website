package com.tientvv.service;

import com.tientvv.model.Order;
import com.tientvv.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

@SuppressWarnings("unused")
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentSyncService {

    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final PayOSService payOSService;

    /**
     * Đồng bộ trạng thái thanh toán từ PayOS về database
     * Chạy mỗi 5 phút để kiểm tra các đơn hàng PENDING_PAYMENT
     */
    @Scheduled(fixedRate = 300000) // 5 phút
    public void syncPaymentStatus() {
        try {
            log.info("Starting payment status sync...");
            
            // Tìm tất cả đơn hàng có trạng thái PENDING_PAYMENT
            List<Order> pendingPaymentOrders = orderRepository.findByOrderStatus("PENDING_PAYMENT");
            
            if (pendingPaymentOrders.isEmpty()) {
                log.info("No pending payment orders found");
                return;
            }
            
            log.info("Found {} pending payment orders to sync", pendingPaymentOrders.size());
            
            for (Order order : pendingPaymentOrders) {
                try {
                    // Kiểm tra xem đơn hàng có transaction code không
                    // (Cần thêm field transactionCode vào Order model)
                    String transactionCode = getTransactionCodeFromOrder(order);
                    
                    if (transactionCode != null) {
                        // Verify với PayOS
                        boolean isPaid = payOSService.verifyPayment(order.getId().toString(), transactionCode);
                        
                        if (isPaid) {
                            // Cập nhật trạng thái thành PAID
                            orderService.updateOrderStatus(order.getId(), "PAID");
                            log.info("Updated order {} status to PAID", order.getId());
                        }
                    }
                } catch (Exception e) {
                    log.error("Error syncing payment status for order {}: {}", order.getId(), e.getMessage());
                }
            }
            
            log.info("Payment status sync completed");
            
        } catch (Exception e) {
            log.error("Error in payment status sync: ", e);
        }
    }

    /**
     * Kiểm tra và hủy các đơn hàng quá hạn (15 phút)
     * Chạy mỗi 30 giây để test nhanh hơn
     * TẠM THỜI TẮT ĐỂ KHÔNG TỰ ĐỘNG CANCEL
     */
    // @Scheduled(fixedRate = 30000) // 30 giây để test nhanh hơn
    public void checkExpiredOrders() {
        try {
            log.info("Starting expired orders check...");
            
            // Tìm tất cả đơn hàng có trạng thái PENDING_PAYMENT và PENDING
            List<Order> pendingPaymentOrders = orderRepository.findByOrderStatus("PENDING_PAYMENT");
            List<Order> pendingOrders = orderRepository.findByOrderStatus("PENDING");
            
            // Gộp cả hai danh sách
            List<Order> allPendingOrders = new java.util.ArrayList<>();
            allPendingOrders.addAll(pendingPaymentOrders);
            allPendingOrders.addAll(pendingOrders);
            
            if (allPendingOrders.isEmpty()) {
                log.info("No pending orders found for expiry check");
                return;
            }
            
            log.info("Found {} PENDING_PAYMENT orders and {} PENDING orders to check for expiry", 
                    pendingPaymentOrders.size(), pendingOrders.size());
            
            for (Order order : allPendingOrders) {
                try {
                    // Kiểm tra xem đơn hàng đã quá 15 phút chưa
                    if (isOrderExpired(order)) {
                        // Cập nhật trạng thái thành CANCELLED
                        orderService.updateOrderStatus(order.getId(), "CANCELLED");
                        log.info("Cancelled expired order {} (older than 15 minutes)", order.getId());
                    }
                } catch (Exception e) {
                    log.error("Error checking expiry for order {}: {}", order.getId(), e.getMessage());
                }
            }
            
            log.info("Expired orders check completed");
            
        } catch (Exception e) {
            log.error("Error in expired orders check: ", e);
        }
    }

    /**
     * Kiểm tra và cập nhật đơn hàng PENDING thường xuyên (mỗi 10 giây)
     * Để xử lý trường hợp user nhấn hủy trên PayOS
     * TẠM THỜI TẮT ĐỂ KHÔNG TỰ ĐỘNG CANCEL
     */
    // @Scheduled(fixedRate = 10000) // 10 giây để kiểm tra thường xuyên
    public void checkPendingOrders() {
        try {
            log.info("Checking pending orders for immediate cancellation...");
            List<Order> pendingPaymentOrders = orderRepository.findByOrderStatus("PENDING_PAYMENT");
            List<Order> pendingOrders = orderRepository.findByOrderStatus("PENDING");
            
            List<Order> allPendingOrders = new ArrayList<>();
            allPendingOrders.addAll(pendingPaymentOrders);
            allPendingOrders.addAll(pendingOrders);
            
            if (allPendingOrders.isEmpty()) {
                log.info("No pending orders found");
                return;
            }
            
            log.info("Found {} pending orders", allPendingOrders.size());
            
            for (Order order : allPendingOrders) {
                try {
                    // Cập nhật ngay lập tức không cần chờ timeout
                    orderService.updateOrderStatus(order.getId(), "CANCELLED");
                    log.info("Immediately cancelled order {} from {} to CANCELLED", 
                            order.getId(), order.getOrderStatus());
                } catch (Exception e) {
                    log.error("Error cancelling order {}: {}", order.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error in checkPendingOrders: {}", e.getMessage());
        }
    }

    // Kiểm tra trạng thái PayOS thường xuyên
    @Scheduled(fixedRate = 30000) // 30 giây kiểm tra một lần
    public void checkPayOSStatus() {
        try {
            log.info("Checking PayOS payment status for pending orders...");
            List<Order> pendingPaymentOrders = orderRepository.findByOrderStatus("PENDING_PAYMENT");
            List<Order> pendingOrders = orderRepository.findByOrderStatus("PENDING");
            
            List<Order> allPendingOrders = new ArrayList<>();
            allPendingOrders.addAll(pendingPaymentOrders);
            allPendingOrders.addAll(pendingOrders);
            
            if (allPendingOrders.isEmpty()) {
                return;
            }
            
            for (Order order : allPendingOrders) {
                try {
                    // Gọi PayOS API để kiểm tra trạng thái
                    String paymentUrl = checkPayOSPaymentStatus(order.getId().toString());
                    if (paymentUrl != null && (paymentUrl.contains("error") || paymentUrl.contains("cancelled"))) {
                        // Nếu PayOS trả về lỗi hoặc cancelled, cập nhật đơn hàng
                        orderService.updateOrderStatus(order.getId(), "CANCELLED");
                        log.info("Updated order {} to CANCELLED based on PayOS status", order.getId());
                    }
                } catch (Exception e) {
                    log.error("Error checking PayOS status for order {}: {}", order.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error in checkPayOSStatus: {}", e.getMessage());
        }
    }

    /**
     * Kiểm tra xem đơn hàng đã quá hạn chưa (15 phút)
     */
    private boolean isOrderExpired(Order order) {
        try {
            // Lấy thời gian hiện tại
            long currentTime = System.currentTimeMillis();
            
            // Lấy thời gian tạo đơn hàng (giả sử orderDate là thời gian tạo)
            long orderTime = order.getOrderDate().toInstant().toEpochMilli();
            
            // Tính thời gian đã trôi qua (15 phút = 900,000 milliseconds - phù hợp với PayOS)
            long timeElapsed = currentTime - orderTime;
            long fifteenMinutesInMs = 15 * 60 * 1000;
            
            log.info("Order {} created at {}, elapsed time: {} ms, threshold: {} ms", 
                    order.getId(), order.getOrderDate(), timeElapsed, fifteenMinutesInMs);
            
            return timeElapsed > fifteenMinutesInMs;
            
        } catch (Exception e) {
            log.error("Error checking order expiry: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Kiểm tra xem đơn hàng đã quá hạn chưa (30 giây)
     */
    private boolean isOrderExpiredQuick(Order order) {
        try {
            // Lấy thời gian hiện tại
            long currentTime = System.currentTimeMillis();
            
            // Lấy thời gian tạo đơn hàng (giả sử orderDate là thời gian tạo)
            long orderTime = order.getOrderDate().toInstant().toEpochMilli();
            
            // Tính thời gian đã trôi qua (30 giây = 30,000 milliseconds - để test nhanh hơn)
            long timeElapsed = currentTime - orderTime;
            long thirtySecondsInMs = 30 * 1000;
            
            log.info("Order {} created at {}, elapsed time: {} ms, threshold: {} ms", 
                    order.getId(), order.getOrderDate(), timeElapsed, thirtySecondsInMs);
            
            return timeElapsed > thirtySecondsInMs;
            
        } catch (Exception e) {
            log.error("Error checking quick order expiry: {}", e.getMessage());
            return false;
        }
    }
    private String getTransactionCodeFromOrder(Order order) {
        return null;
    }

    private String checkPayOSPaymentStatus(String orderCode) {
        try {
            // Gọi PayOS API để kiểm tra trạng thái payment
            return null;
        } catch (Exception e) {
            log.error("Error checking PayOS payment status: {}", e.getMessage());
            return null;
        }
    }
} 