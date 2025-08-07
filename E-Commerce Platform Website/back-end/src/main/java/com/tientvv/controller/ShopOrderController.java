package com.tientvv.controller;

import com.tientvv.dto.order.OrderDto;
import com.tientvv.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/api/shop/orders")
@RequiredArgsConstructor
@Slf4j
public class ShopOrderController {

  private final OrderService orderService;

  @GetMapping
  public ResponseEntity<Map<String, Object>> getShopOrders(
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String paymentMethod) {

    try {
      log.info("Getting shop orders with filters - status: {}, paymentMethod: {}",
          status, paymentMethod);

      List<OrderDto> orders = orderService.getShopOrdersWithFilters(status, paymentMethod, null, null);
      Map<String, Object> statistics = orderService.getShopOrderStatistics();

      Map<String, Object> response = Map.of(
          "success", true,
          "orders", orders,
          "statistics", statistics,
          "total", orders.size());

      log.info("Found {} orders for shop", orders.size());
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("Error getting shop orders: ", e);
      return ResponseEntity.badRequest().body(Map.of(
          "success", false,
          "message", "Lỗi lấy danh sách đơn hàng: " + e.getMessage()));
    }
  }

  @GetMapping("/{orderId}")
  public ResponseEntity<Map<String, Object>> getShopOrderById(@PathVariable UUID orderId) {
    try {
      log.info("Getting shop order by ID: {}", orderId);

      OrderDto order = orderService.getShopOrderById(orderId);

      return ResponseEntity.ok(Map.of(
          "success", true,
          "order", order));

    } catch (Exception e) {
      log.error("Error getting shop order by ID: {}", orderId, e);
      return ResponseEntity.badRequest().body(Map.of(
          "success", false,
          "message", "Lỗi lấy thông tin đơn hàng: " + e.getMessage()));
    }
  }

  @PutMapping("/{orderId}/status")
  public ResponseEntity<Map<String, Object>> updateOrderStatus(
      @PathVariable UUID orderId,
      @RequestBody Map<String, String> request) {

    try {
      String status = request.get("status");
      log.info("Updating order {} status to: {}", orderId, status);

      if (status == null || status.trim().isEmpty()) {
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "message", "Trạng thái đơn hàng không được để trống"));
      }

      OrderDto updatedOrder = orderService.updateShopOrderStatus(orderId, status);

      return ResponseEntity.ok(Map.of(
          "success", true,
          "message", "Cập nhật trạng thái đơn hàng thành công",
          "order", updatedOrder));

    } catch (Exception e) {
      log.error("Error updating order {} status: ", orderId, e);
      return ResponseEntity.badRequest().body(Map.of(
          "success", false,
          "message", "Lỗi cập nhật trạng thái đơn hàng: " + e.getMessage()));
    }
  }

  @GetMapping("/statistics")
  public ResponseEntity<Map<String, Object>> getShopOrderStatistics() {
    try {
      log.info("Getting shop order statistics");

      Map<String, Object> statistics = orderService.getShopOrderStatistics();

      return ResponseEntity.ok(Map.of(
          "success", true,
          "statistics", statistics));

    } catch (Exception e) {
      log.error("Error getting shop order statistics: ", e);
      return ResponseEntity.badRequest().body(Map.of(
          "success", false,
          "message", "Lỗi lấy thống kê đơn hàng: " + e.getMessage()));
    }
  }
}