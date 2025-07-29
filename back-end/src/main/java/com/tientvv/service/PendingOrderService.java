package com.tientvv.service;

import com.tientvv.dto.order.CreateOrderDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class PendingOrderService {

  // Lưu trữ đơn hàng tạm thời trong memory (trong production nên dùng Redis)
  private final Map<String, PendingOrderData> pendingOrders = new ConcurrentHashMap<>();

  public String createPendingOrder(CreateOrderDto orderData) {
    try {
      // Tạo order code duy nhất
      String orderCode = "ORDER_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

      // Lưu thông tin đơn hàng tạm thời
      PendingOrderData pendingOrder = new PendingOrderData();
      pendingOrder.setOrderCode(orderCode);
      pendingOrder.setOrderData(orderData);
      pendingOrder.setCreatedAt(System.currentTimeMillis());
      pendingOrder.setStatus("PENDING_PAYMENT");

      pendingOrders.put(orderCode, pendingOrder);

      log.info("Created pending order: {}", orderCode);
      return orderCode;

    } catch (Exception e) {
      log.error("Error creating pending order: ", e);
      throw new RuntimeException("Lỗi tạo đơn hàng tạm thời: " + e.getMessage());
    }
  }

  public PendingOrderData getPendingOrder(String orderCode) {
    return pendingOrders.get(orderCode);
  }

  public void removePendingOrder(String orderCode) {
    pendingOrders.remove(orderCode);
    log.info("Removed pending order: {}", orderCode);
  }

  public void updatePendingOrderStatus(String orderCode, String status) {
    PendingOrderData pendingOrder = pendingOrders.get(orderCode);
    if (pendingOrder != null) {
      pendingOrder.setStatus(status);
      log.info("Updated pending order {} status to: {}", orderCode, status);
    }
  }

  // Inner class để lưu trữ thông tin đơn hàng tạm thời
  public static class PendingOrderData {
    private String orderCode;
    private CreateOrderDto orderData;
    private long createdAt;
    private String status;

    // Getters and Setters
    public String getOrderCode() {
      return orderCode;
    }

    public void setOrderCode(String orderCode) {
      this.orderCode = orderCode;
    }

    public CreateOrderDto getOrderData() {
      return orderData;
    }

    public void setOrderData(CreateOrderDto orderData) {
      this.orderData = orderData;
    }

    public long getCreatedAt() {
      return createdAt;
    }

    public void setCreatedAt(long createdAt) {
      this.createdAt = createdAt;
    }

    public String getStatus() {
      return status;
    }

    public void setStatus(String status) {
      this.status = status;
    }
  }
}