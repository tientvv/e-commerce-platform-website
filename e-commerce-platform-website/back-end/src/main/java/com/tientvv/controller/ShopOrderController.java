package com.tientvv.controller;

import com.tientvv.dto.order.OrderDto;
import com.tientvv.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
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
  private final com.tientvv.service.InvoiceService invoiceService;

  @GetMapping
  public ResponseEntity<Map<String, Object>> getShopOrders(
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String paymentMethod,
      @RequestParam(required = false) String timeFilter,
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate) {

    try {
      log.info("Getting shop orders with filters - status: {}, paymentMethod: {}, timeFilter: {}, startDate: {}, endDate: {}",
          status, paymentMethod, timeFilter, startDate, endDate);

             // Xử lý time filter
       LocalDate startDateFilter = null;
       LocalDate endDateFilter = null;
       LocalDate today = LocalDate.now();
       
               // Ưu tiên startDate và endDate từ params nếu có
        if (startDate != null && !startDate.isEmpty() && !startDate.equals("null")) {
          try {
            startDateFilter = LocalDate.parse(startDate);
          } catch (Exception e) {
            log.error("Error parsing startDate: {}", startDate, e);
          }
        }
        if (endDate != null && !endDate.isEmpty() && !endDate.equals("null")) {
          try {
            endDateFilter = LocalDate.parse(endDate);
          } catch (Exception e) {
            log.error("Error parsing endDate: {}", endDate, e);
          }
        }
       
       // Nếu không có startDate/endDate, sử dụng timeFilter
       if (startDateFilter == null && timeFilter != null && !timeFilter.equals("ALL")) {
         switch (timeFilter) {
           case "TODAY":
             startDateFilter = today;
             endDateFilter = today;
             break;
           case "THIS_WEEK":
             startDateFilter = today.minusDays(today.getDayOfWeek().getValue() - 1);
             endDateFilter = today;
             break;
           case "THIS_MONTH":
             startDateFilter = today.withDayOfMonth(1);
             endDateFilter = today;
             break;
         }
       }
       
       // Nếu vẫn không có endDate, dùng hôm nay
       if (endDateFilter == null) {
         endDateFilter = today;
       }

      List<OrderDto> orders = orderService.getShopOrdersWithFilters(status, paymentMethod, startDateFilter, endDateFilter);
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

  @PutMapping("/{orderId}/payment-status")
  public ResponseEntity<Map<String, Object>> updatePaymentStatus(
      @PathVariable UUID orderId,
      @RequestBody Map<String, String> request) {

    try {
      String paymentStatus = request.get("paymentStatus");
      log.info("Updating order {} payment status to: {}", orderId, paymentStatus);

      if (paymentStatus == null || paymentStatus.trim().isEmpty()) {
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "message", "Trạng thái thanh toán không được để trống"));
      }

      // Validate payment status values
      List<String> validPaymentStatuses = Arrays.asList(
          "PENDING", "SUCCESS", "FAILED", "CANCELLED", "REFUNDED"
      );

      if (!validPaymentStatuses.contains(paymentStatus)) {
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "message", "Trạng thái thanh toán không hợp lệ. Các trạng thái hợp lệ: " + validPaymentStatuses));
      }

      OrderDto updatedOrder = orderService.updateTransactionStatus(orderId, paymentStatus);

      return ResponseEntity.ok(Map.of(
          "success", true,
          "message", "Cập nhật trạng thái thanh toán thành công",
          "order", updatedOrder));

    } catch (Exception e) {
      log.error("Error updating order {} payment status: ", orderId, e);
      return ResponseEntity.badRequest().body(Map.of(
          "success", false,
          "message", "Lỗi cập nhật trạng thái thanh toán: " + e.getMessage()));
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

  @GetMapping("/revenue-statistics")
  public ResponseEntity<Map<String, Object>> getRevenueStatistics(
      @RequestParam(required = false) String period,
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate) {
    try {
      log.info("Getting revenue statistics - period: {}, startDate: {}, endDate: {}", period, startDate, endDate);
      log.info("DEBUG: Raw parameters - period: '{}', startDate: '{}', endDate: '{}'", period, startDate, endDate);

      Map<String, Object> revenueStats = orderService.getRevenueStatistics(period, startDate, endDate);

      return ResponseEntity.ok(Map.of(
          "success", true,
          "revenueStatistics", revenueStats));

    } catch (Exception e) {
      log.error("Error getting revenue statistics: ", e);
      return ResponseEntity.badRequest().body(Map.of(
          "success", false,
          "message", "Lỗi lấy thống kê doanh thu: " + e.getMessage()));
    }
  }

  @PutMapping("/{orderId}/cancel")
  public ResponseEntity<Map<String, Object>> cancelOrder(@PathVariable UUID orderId) {
    try {
      log.info("Cancelling order: {}", orderId);

      OrderDto cancelledOrder = orderService.cancelOrder(orderId);

      return ResponseEntity.ok(Map.of(
          "success", true,
          "message", "Hủy đơn hàng thành công",
          "order", cancelledOrder));

    } catch (Exception e) {
      log.error("Error cancelling order {}: ", orderId, e);
      return ResponseEntity.badRequest().body(Map.of(
          "success", false,
          "message", "Lỗi hủy đơn hàng: " + e.getMessage()));
    }
  }

  @GetMapping("/{orderId}/invoice")
  public ResponseEntity<String> exportInvoice(@PathVariable UUID orderId) {
    try {
      log.info("Exporting invoice for order: {}", orderId);

      // Lấy thông tin đơn hàng
      OrderDto order = orderService.getShopOrderById(orderId);
      
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
      log.error("Error exporting invoice for order {}: ", orderId, e);
      return ResponseEntity.badRequest().body("Không thể tạo hóa đơn: " + e.getMessage());
    }
  }
}