package com.tientvv.controller;

import com.tientvv.dto.order.CreateOrderDto;
import com.tientvv.dto.order.OrderDto;
import com.tientvv.model.Account;
import com.tientvv.service.AccountService;
import com.tientvv.service.OrderService;
import com.tientvv.service.InvoiceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

  @Autowired
  private AccountService accountService;

  @Autowired
  private InvoiceService invoiceService;

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
      
      // Debug: Log order items variant information
      if (order.getOrderItems() != null) {
        System.out.println("Order items count: " + order.getOrderItems().size());
        for (int i = 0; i < order.getOrderItems().size(); i++) {
          OrderDto.OrderItemDto item = order.getOrderItems().get(i);
          System.out.println("Item " + (i + 1) + ":");
          System.out.println("  - ProductName: " + item.getProductName());
          System.out.println("  - VariantName: " + item.getVariantName());
          System.out.println("  - VariantValue: " + item.getVariantValue());
          System.out.println("  - ProductVariantId: " + item.getProductVariantId());
        }
      }
      
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

  @GetMapping("/my-orders")
  public ResponseEntity<Map<String, Object>> getMyOrders() {
    try {
      // Lấy user hiện tại từ session
      Account currentUser = accountService.getCurrentUserFromSession();
      if (currentUser == null) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Bạn cần phải đăng nhập để xem đơn hàng");
        return ResponseEntity.badRequest().body(errorResponse);
      }

      List<OrderDto> orders = orderService.getOrdersByAccountId(currentUser.getId());
      
      // Debug: Log variant information for all orders
      System.out.println("=== DEBUG MY ORDERS ===");
      System.out.println("Total orders: " + orders.size());
      for (int orderIndex = 0; orderIndex < orders.size(); orderIndex++) {
        OrderDto order = orders.get(orderIndex);
        System.out.println("Order " + (orderIndex + 1) + " (ID: " + order.getId() + "):");
        if (order.getOrderItems() != null) {
          System.out.println("  Order items count: " + order.getOrderItems().size());
          for (int itemIndex = 0; itemIndex < order.getOrderItems().size(); itemIndex++) {
            OrderDto.OrderItemDto item = order.getOrderItems().get(itemIndex);
            System.out.println("    Item " + (itemIndex + 1) + ":");
            System.out.println("      - ProductName: " + item.getProductName());
            System.out.println("      - VariantName: " + item.getVariantName());
            System.out.println("      - VariantValue: " + item.getVariantValue());
            System.out.println("      - ProductVariantId: " + item.getProductVariantId());
          }
        }
      }
      System.out.println("=== END DEBUG MY ORDERS ===");
      
      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("orders", orders);
      response.put("message", "Lấy danh sách đơn hàng thành công");
      
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("message", "Lỗi lấy danh sách đơn hàng: " + e.getMessage());
      return ResponseEntity.badRequest().body(errorResponse);
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

  @PostMapping("/check-inventory")
  public ResponseEntity<Map<String, Object>> checkInventory(@RequestBody CreateOrderDto createOrderDto) {
    try {
      // Kiểm tra số lượng tồn kho
      orderService.validateProductQuantities(createOrderDto.getOrderItems());
      
      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("message", "Số lượng sản phẩm đủ để đặt hàng");
      return ResponseEntity.ok(response);
      
    } catch (Exception e) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("message", e.getMessage());
      return ResponseEntity.badRequest().body(errorResponse);
    }
  }

  @PostMapping("/create-sample-order")
  public ResponseEntity<Map<String, Object>> createSampleOrder() {
    try {
      // Lấy user hiện tại từ session
      Account currentUser = accountService.getCurrentUserFromSession();
      if (currentUser == null) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Bạn cần phải đăng nhập để tạo đơn hàng");
        return ResponseEntity.badRequest().body(errorResponse);
      }

      // Tạo đơn hàng mẫu
      OrderDto sampleOrder = orderService.createSampleOrder(currentUser.getId());
      
      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("message", "Tạo đơn hàng mẫu thành công");
      response.put("order", sampleOrder);
      
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("message", "Lỗi tạo đơn hàng mẫu: " + e.getMessage());
      return ResponseEntity.badRequest().body(errorResponse);
    }
  }

  @GetMapping("/{orderId}/invoice")
  public ResponseEntity<String> exportInvoice(@PathVariable UUID orderId) {
    try {
      // Lấy user hiện tại từ session
      Account currentUser = accountService.getCurrentUserFromSession();
      if (currentUser == null) {
        return ResponseEntity.badRequest().body("Bạn cần đăng nhập để xem hóa đơn");
      }

      // Lấy thông tin đơn hàng
      OrderDto order = orderService.getOrderById(orderId);
      
      // Kiểm tra xem đơn hàng có thuộc về user hiện tại không
      if (!order.getAccountId().equals(currentUser.getId())) {
        return ResponseEntity.badRequest().body("Bạn không có quyền xem hóa đơn này");
      }

      // Tạo hóa đơn HTML
      String htmlContent = invoiceService.generateInvoicePdf(order);

      // Set headers
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.TEXT_HTML);
      headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

      return ResponseEntity.ok()
          .headers(headers)
          .body(htmlContent);

    } catch (Exception e) {
      System.err.println("Error exporting invoice: " + e.getMessage());
      e.printStackTrace();
      return ResponseEntity.badRequest().body("Không thể tạo hóa đơn: " + e.getMessage());
    }
  }
}