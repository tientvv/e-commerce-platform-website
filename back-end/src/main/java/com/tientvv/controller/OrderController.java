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
  public ResponseEntity<OrderDto> getOrderById(@PathVariable UUID orderId) {
    try {
      OrderDto order = orderService.getOrderById(orderId);
      return ResponseEntity.ok(order);
    } catch (Exception e) {
      return ResponseEntity.notFound().build();
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
  public ResponseEntity<OrderDto> updateOrderStatus(
      @PathVariable UUID orderId,
      @RequestParam String status) {
    try {
      OrderDto order = orderService.updateOrderStatus(orderId, status);
      return ResponseEntity.ok(order);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

}