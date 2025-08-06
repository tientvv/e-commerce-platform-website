package com.tientvv.controller;

import com.tientvv.dto.order.CreateOrderDto;
import com.tientvv.dto.order.OrderDto;
import com.tientvv.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Arrays;
import java.util.HashMap;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

  @Autowired
  private OrderService orderService;

  @PostMapping
  public ResponseEntity<?> createOrder(@Valid @RequestBody CreateOrderDto createOrderDto) {
    try {
      OrderDto order = orderService.createOrder(createOrderDto);
      return ResponseEntity.ok(order);
    } catch (Exception e) {
      System.err.println("Error creating order: " + e.getMessage());
      e.printStackTrace();
      return ResponseEntity.badRequest().body(Map.of("message", "Lỗi tạo đơn hàng: " + e.getMessage()));
    }
  }

  @GetMapping("/{orderId}")
  public ResponseEntity<Map<String, Object>> getOrderById(@PathVariable UUID orderId) {
    try {
      System.out.println("Getting order by ID: " + orderId);
      OrderDto order = orderService.getOrderById(orderId);
      System.out.println("Found order: " + order.getId() + ", status: " + order.getOrderStatus());
      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("data", order);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      System.err.println("Error getting order: " + e.getMessage());
      e.printStackTrace();
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("message", "Order not found: " + e.getMessage());
      return ResponseEntity.badRequest().body(errorResponse);
    }
  }

  @GetMapping("/account/{accountId}")
  public ResponseEntity<List<OrderDto>> getOrdersByAccountId(@PathVariable UUID accountId) {
    try {
      List<OrderDto> orders = orderService.getOrdersByAccountId(accountId);
      return ResponseEntity.ok(orders);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/all")
  public ResponseEntity<List<OrderDto>> getAllOrders() {
    try {
      List<OrderDto> orders = orderService.getAllOrders();
      return ResponseEntity.ok(orders);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/shop/{shopId}")
  public ResponseEntity<List<OrderDto>> getOrdersByShopId(@PathVariable UUID shopId) {
    try {
      List<OrderDto> orders = orderService.getOrdersByShopId(shopId);
      return ResponseEntity.ok(orders);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PutMapping("/{orderId}/status")
  public ResponseEntity<Map<String, Object>> updateOrderStatus(
      @PathVariable UUID orderId,
      @RequestBody Map<String, String> request) {
    try {
      String newStatus = request.get("status");
      
      if (newStatus == null) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Status là bắt buộc");
        return ResponseEntity.badRequest().body(errorResponse);
      }
      
      // Validate status values
      List<String> validStatuses = Arrays.asList(
          "PENDING_PROCESSING", "PROCESSED", "READY_FOR_PICKUP", 
          "IN_TRANSIT", "DELIVERED", "CANCELLED"
      );
      
      if (!validStatuses.contains(newStatus)) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Status không hợp lệ. Các status hợp lệ: " + validStatuses);
        return ResponseEntity.badRequest().body(errorResponse);
      }
      
      OrderDto updatedOrder = orderService.updateOrderStatus(orderId, newStatus);
      
      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("message", "Cập nhật trạng thái đơn hàng thành công");
      response.put("data", updatedOrder);
      
      return ResponseEntity.ok(response);
      
    } catch (Exception e) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("message", "Lỗi cập nhật trạng thái: " + e.getMessage());
      return ResponseEntity.badRequest().body(errorResponse);
    }
  }

}