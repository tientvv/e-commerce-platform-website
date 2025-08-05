package com.tientvv.service;

import com.tientvv.model.Order;
import com.tientvv.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentExpiryService {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    /**
     * Kiểm tra và hủy đơn hàng hết hạn mỗi 2 phút
     * PayOS có thời gian hết hạn 15 phút (900 giây)
     */
    @Scheduled(fixedRate = 120000) // 2 phút = 120,000 ms
    public void cancelExpiredOrdersScheduled() {
        cancelExpiredOrders();
    }
    
    /**
     * Kiểm tra và hủy đơn hàng hết hạn (có thể gọi từ bên ngoài)
     */
    public void cancelExpiredOrders() {
        try {
            log.info("🕐 Checking for expired orders...");
            
            // Lấy tất cả đơn hàng PENDING_PAYMENT và PENDING
            List<Order> pendingPaymentOrders = orderRepository.findByOrderStatus("PENDING_PAYMENT");
            List<Order> pendingOrders = orderRepository.findByOrderStatus("PENDING");
            
            int totalExpired = 0;
            long currentTime = System.currentTimeMillis();
            
            // Kiểm tra đơn hàng PENDING_PAYMENT
            for (Order order : pendingPaymentOrders) {
                if (isOrderExpired(order, currentTime)) {
                    orderService.updateOrderStatus(order.getId(), "CANCELLED");
                    totalExpired++;
                    log.info("⏰ Cancelled expired PENDING_PAYMENT order: {} (created: {})", 
                            order.getId(), order.getOrderDate());
                }
            }
            
            // Kiểm tra đơn hàng PENDING
            for (Order order : pendingOrders) {
                if (isOrderExpired(order, currentTime)) {
                    orderService.updateOrderStatus(order.getId(), "CANCELLED");
                    totalExpired++;
                    log.info("⏰ Cancelled expired PENDING order: {} (created: {})", 
                            order.getId(), order.getOrderDate());
                }
            }
            
            if (totalExpired > 0) {
                log.info("✅ Cancelled {} expired orders", totalExpired);
            } else {
                log.info("✅ No expired orders found");
            }
            
        } catch (Exception e) {
            log.error("❌ Error checking expired orders: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Kiểm tra xem đơn hàng có hết hạn chưa (15 phút)
     */
    private boolean isOrderExpired(Order order, long currentTime) {
        try {
            // Chuyển đổi OrderDate (OffsetDateTime) thành Instant
            OffsetDateTime orderDate = order.getOrderDate();
            Instant orderInstant = orderDate.toInstant();
            long orderTime = orderInstant.toEpochMilli();
            
            // Tính thời gian đã trôi qua (tính bằng giây)
            long timeElapsed = currentTime - orderTime;
            long timeElapsedSeconds = timeElapsed / 1000;
            
            // Hết hạn sau 15 phút (900 giây)
            boolean isExpired = timeElapsedSeconds > 900;
            
            if (isExpired) {
                log.debug("⏰ Order {} expired: {} seconds elapsed (limit: 900s)", 
                        order.getId(), timeElapsedSeconds);
            }
            
            return isExpired;
            
        } catch (Exception e) {
            log.error("❌ Error checking expiry for order {}: {}", order.getId(), e.getMessage());
            return false;
        }
    }
} 