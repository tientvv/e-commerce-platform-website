package com.tientvv.service;

import com.tientvv.dto.order.CreateOrderDto;
import com.tientvv.dto.order.OrderDto;
import com.tientvv.model.*;
import com.tientvv.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.math.RoundingMode;

@Service
@Slf4j
public class OrderService {

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private OrderItemRepository orderItemRepository;

  @Autowired
  private TransactionRepository transactionRepository;

  @Autowired
  private AccountRepository accountRepository;

  @Autowired
  private ShopRepository shopRepository;

  @Autowired
  private ShippingRepository shippingRepository;

  @Autowired
  private PaymentRepository paymentRepository;

  @Autowired
  private ProductVariantRepository productVariantRepository;

  @Autowired
  private ShopService shopService;

  @Autowired
  private AccountService accountService;

  @Autowired
  private EmailService emailService;

  @SuppressWarnings("unused")
  @Autowired
  private DiscountRepository discountRepository;

  /**
   * Kiểm tra số lượng tồn kho trước khi đặt hàng
   */
  public void validateProductQuantities(List<CreateOrderDto.OrderItemDto> orderItems) {
    for (CreateOrderDto.OrderItemDto itemDto : orderItems) {
      if (itemDto.getProductVariantId() != null) {
        ProductVariant productVariant = productVariantRepository.findById(itemDto.getProductVariantId())
            .orElseThrow(() -> new RuntimeException("Product variant not found: " + itemDto.getProductVariantId()));

        Integer currentQuantity = productVariant.getQuantity();
        Integer orderedQuantity = itemDto.getQuantity();

        if (currentQuantity == null || currentQuantity < orderedQuantity) {
          throw new RuntimeException("Không đủ số lượng sản phẩm: " +
              productVariant.getProduct().getName() + " - " +
              productVariant.getVariantName() + ": " + productVariant.getVariantValue() +
              ". Cần: " + orderedQuantity + ", Có: " + currentQuantity);
        }
      }
    }
  }

  @Transactional
  public OrderDto createOrder(CreateOrderDto createOrderDto) {
    // Validate entities exist
    Account account = accountRepository.findById(createOrderDto.getAccountId())
        .orElseThrow(() -> new RuntimeException("Account not found"));

    // Kiểm tra số lượng tồn kho trước khi tạo đơn hàng
    validateProductQuantities(createOrderDto.getOrderItems());

    Shop shop = shopRepository.findById(createOrderDto.getShopId())
        .orElseGet(() -> {
          try {
            // Nếu không tìm thấy shop, tạo shop demo
            System.out.println("Shop not found, creating demo shop...");
            return shopService.createDemoShop();
          } catch (Exception e) {
            System.err.println("Error creating demo shop: " + e.getMessage());
            throw new RuntimeException("Shop not found and cannot create demo shop");
          }
        });

    Shipping shipping = shippingRepository.findById(createOrderDto.getShippingId())
        .orElseThrow(() -> new RuntimeException("Shipping not found"));

    Payment payment = paymentRepository.findById(createOrderDto.getPaymentId())
        .orElseThrow(() -> new RuntimeException("Payment not found"));

    // Create order
    Order order = new Order();
    order.setAccount(account);
    order.setShop(shop);
    order.setShipping(shipping);
    order.setPayment(payment);
    order.setTotalAmount(createOrderDto.getTotalAmount());
    order.setDiscountAmount(
        createOrderDto.getDiscountAmount() != null ? createOrderDto.getDiscountAmount() : BigDecimal.ZERO);

    // Set order status based on payment method
    if ("PAYOS".equals(payment.getPaymentCode())) {
      order.setOrderStatus("PENDING_PROCESSING");
    } else {
      // COD - thanh toán khi nhận hàng, trừ số lượng ngay lập tức
      order.setOrderStatus("PENDING_PROCESSING");
    }

    order.setOrderDate(OffsetDateTime.now());
    order.setShippingAddress(createOrderDto.getShippingAddress());

    // Save order
    Order savedOrder = orderRepository.save(order);

    // Set orderCode sau khi đã có ID
    if ("PAYOS".equals(payment.getPaymentCode())) {
      // Tạo orderCode cho PayOS (sử dụng hashCode của UUID)
      String orderCode = String.valueOf(Math.abs(savedOrder.getId().hashCode()));
      savedOrder.setOrderCode(orderCode);
      orderRepository.save(savedOrder);
    } else {
      savedOrder.setOrderCode(savedOrder.getId().toString());
      orderRepository.save(savedOrder);
    }

    // Create order items
    List<OrderItem> orderItems = createOrderDto.getOrderItems().stream()
        .map(itemDto -> {
          OrderItem orderItem = new OrderItem();
          orderItem.setOrder(savedOrder);

          // Handle product variant (can be null for products without variants)
          if (itemDto.getProductVariantId() != null) {
            ProductVariant productVariant = productVariantRepository.findById(itemDto.getProductVariantId())
                .orElseThrow(() -> new RuntimeException("Product variant not found"));
            orderItem.setProductVariant(productVariant);
          }

          orderItem.setQuantity(itemDto.getQuantity());
          orderItem.setProductPrice(itemDto.getProductPrice());
          orderItem.setDiscountApplied(
              itemDto.getDiscountApplied() != null ? itemDto.getDiscountApplied() : BigDecimal.ZERO);

          return orderItemRepository.save(orderItem);
        })
        .collect(Collectors.toList());

    // Trừ số lượng sản phẩm cho COD ngay lập tức
    if (!"PAYOS".equals(payment.getPaymentCode())) {
      deductProductQuantities(savedOrder.getId());
    }

    // Create initial transaction
    Transaction transaction = new Transaction();
    transaction.setOrder(savedOrder);
    transaction.setPayment(payment);
    transaction.setTransactionAmount(createOrderDto.getTotalAmount());
    transaction.setTransactionDate(OffsetDateTime.now());

    // Set transaction status based on payment method
    if ("PAYOS".equals(payment.getPaymentCode())) {
      transaction.setTransactionStatus("PENDING");
      transaction.setTransactionCode("PAYOS_" + savedOrder.getId());
    } else {
      transaction.setTransactionStatus("PENDING");
      transaction.setTransactionCode("COD_" + savedOrder.getId());
    }

    transactionRepository.save(transaction);

    // Get order items and transactions for DTO
    List<Transaction> transactions = transactionRepository.findByOrderId(savedOrder.getId());

    // Convert to DTO
    OrderDto orderDto = convertToDto(savedOrder, orderItems, transactions);

    // Send order confirmation email
    try {
      log.info("Attempting to send order confirmation email for order {}", savedOrder.getId());
      emailService.sendOrderConfirmationEmail(orderDto);
      log.info("Order confirmation email sent successfully for order {}", savedOrder.getId());
    } catch (Exception e) {
      log.error("Error sending order confirmation email for order {}: {}", savedOrder.getId(), e.getMessage(), e);
      // Don't fail the order creation if email fails
    }

    return orderDto;
  }

  public OrderDto getOrderById(UUID orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new RuntimeException("Order not found"));

    List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
    List<Transaction> transactions = transactionRepository.findByOrderId(orderId);

    return convertToDto(order, orderItems, transactions);
  }

  public List<OrderDto> getOrdersByAccountId(UUID accountId) {
    List<Order> orders = orderRepository.findByAccountIdOrderByOrderDateDesc(accountId);
    return orders.stream()
        .map(order -> {
          List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
          List<Transaction> transactions = transactionRepository.findByOrderId(order.getId());
          return convertToDto(order, orderItems, transactions);
        })
        .sorted((a, b) -> {
          // Sắp xếp theo thời gian đặt hàng mới nhất
          if (a.getOrderDate() == null && b.getOrderDate() == null) return 0;
          if (a.getOrderDate() == null) return 1;
          if (b.getOrderDate() == null) return -1;
          return b.getOrderDate().compareTo(a.getOrderDate()); // Mới nhất trước
        })
        .collect(Collectors.toList());
  }

  public List<OrderDto> getOrdersByShopId(UUID shopId) {
    List<Order> orders = orderRepository.findByShopIdOrderByOrderDateDesc(shopId);
    return orders.stream()
        .map(order -> {
          List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
          List<Transaction> transactions = transactionRepository.findByOrderId(order.getId());
          return convertToDto(order, orderItems, transactions);
        })
        .collect(Collectors.toList());
  }

  public List<OrderDto> getAllOrders() {
    List<Order> orders = orderRepository.findAll();
    return orders.stream()
        .map(order -> {
          List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
          List<Transaction> transactions = transactionRepository.findByOrderId(order.getId());
          return convertToDto(order, orderItems, transactions);
        })
        .collect(Collectors.toList());
  }

  @Transactional
  public OrderDto updateOrderStatus(UUID orderId, String status) {
    return updateOrderStatus(orderId, status, true);
  }

  @Transactional
  public OrderDto updateOrderStatus(UUID orderId, String status, boolean sendEmail) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new RuntimeException("Order not found"));

    String oldStatus = order.getOrderStatus();
    log.info("Updating order {} status from {} to {} (sendEmail: {})", orderId, oldStatus, status, sendEmail);
    
    order.setOrderStatus(status);

    if ("DELIVERED".equals(status)) {
      order.setDeliveredDate(OffsetDateTime.now());
      log.info("Order {} marked as delivered at {}", orderId, order.getDeliveredDate());
    } else if ("CANCELLED".equals(status)) {
      order.setCancelledDate(OffsetDateTime.now());
      log.info("Order {} marked as cancelled at {}", orderId, order.getCancelledDate());
      
      // Hoàn trả số lượng sản phẩm khi hủy đơn hàng
      // Với COD: số lượng đã được trừ khi tạo đơn hàng
      // Với PayOS: số lượng đã được trừ khi thanh toán thành công
      restoreProductQuantities(orderId);
      
      // Cập nhật trạng thái thanh toán thành CANCELLED khi hủy đơn hàng
      List<Transaction> transactions = transactionRepository.findByOrderId(orderId);
      for (Transaction transaction : transactions) {
        transaction.setTransactionStatus("CANCELLED");
        transaction.setTransactionDate(OffsetDateTime.now());
        transactionRepository.save(transaction);
        log.info("Transaction {} cancelled for order {}", transaction.getId(), orderId);
      }
    }

    order = orderRepository.save(order);
    log.info("Order {} status updated successfully to {}", orderId, status);

    List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
    List<Transaction> updatedTransactions = transactionRepository.findByOrderId(orderId);
    OrderDto orderDto = convertToDto(order, orderItems, updatedTransactions);

    // Send email notifications based on status change (only if sendEmail = true)
    if (sendEmail) {
      try {
        log.info("Attempting to send email notification for order {} status change from {} to {}", 
            orderId, oldStatus, status);
        
        if ("CANCELLED".equals(status)) {
          log.info("Sending cancellation email for order {}", orderId);
          emailService.sendOrderCancellationEmail(orderDto);
          log.info("Cancellation email sent successfully for order {}", orderId);
        } else if ("DELIVERED".equals(status)) {
          log.info("Sending delivery email for order {}", orderId);
          emailService.sendOrderDeliveryEmail(orderDto);
          log.info("Delivery email sent successfully for order {}", orderId);
        } else if (!oldStatus.equals(status)) {
          log.info("Sending status update email for order {} from {} to {}", orderId, oldStatus, status);
          emailService.sendOrderStatusUpdateEmail(orderDto, oldStatus, status);
          log.info("Status update email sent successfully for order {}", orderId);
        }
      } catch (Exception e) {
        log.error("Error sending order status update email for order {}: {}", orderId, e.getMessage(), e);
        // Don't fail the status update if email fails, but log the error
      }
    } else {
      log.info("Skipping email notification for order {} status change (sendEmail=false)", orderId);
    }

    return orderDto;
  }

  @Transactional
  public OrderDto updateTransactionStatus(UUID orderId, String transactionStatus) {
    return updateTransactionStatus(orderId, transactionStatus, true);
  }

  @Transactional
  public OrderDto updateTransactionStatus(UUID orderId, String transactionStatus, boolean sendEmail) {
    try {
      Order order = orderRepository.findById(orderId)
          .orElseThrow(() -> {
            log.error("Order {} not found for transaction status update", orderId);
            return new RuntimeException("Không tìm thấy đơn hàng");
          });

      List<Transaction> transactions = transactionRepository.findByOrderId(orderId);
      if (transactions.isEmpty()) {
        log.error("No transactions found for order {}", orderId);
        throw new RuntimeException("Không tìm thấy thông tin thanh toán cho đơn hàng");
      }

      Transaction transaction = transactions.get(0);
      String oldTransactionStatus = transaction.getTransactionStatus();
      String oldOrderStatus = order.getOrderStatus();

      log.info("Updating transaction status for order {} from {} to {} (sendEmail: {})", 
          orderId, oldTransactionStatus, transactionStatus, sendEmail);

      transaction.setTransactionStatus(transactionStatus);
      transaction.setTransactionDate(OffsetDateTime.now());
      transactionRepository.save(transaction);

      // Cập nhật trạng thái đơn hàng dựa trên trạng thái thanh toán
      if ("SUCCESS".equals(transactionStatus)) {
        // Nếu transaction chuyển từ PENDING sang SUCCESS, trừ số lượng sản phẩm
        // Chỉ trừ số lượng cho PayOS, không trừ cho COD vì đã trừ khi tạo đơn hàng
        if ("PENDING".equals(oldTransactionStatus) && "PAYOS".equals(order.getPayment().getPaymentCode())) {
          deductProductQuantities(orderId);
        }
        order.setOrderStatus("PROCESSED");
        log.info("Order {} status updated to PROCESSED due to successful payment", orderId);
      } else if ("CANCELLED".equals(transactionStatus)) {
        // Hoàn trả số lượng sản phẩm khi hủy transaction
        // Với COD: số lượng đã được trừ khi tạo đơn hàng
        // Với PayOS: số lượng đã được trừ khi thanh toán thành công
        restoreProductQuantities(orderId);
        order.setOrderStatus("CANCELLED");
        order.setCancelledDate(OffsetDateTime.now());
        log.info("Order {} status updated to CANCELLED due to cancelled payment", orderId);
      }

      order = orderRepository.save(order);
      log.info("Transaction status updated successfully for order {}", orderId);

      List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
      List<Transaction> updatedTransactions = transactionRepository.findByOrderId(orderId);
      OrderDto orderDto = convertToDto(order, orderItems, updatedTransactions);

      // Send email notifications for transaction status changes (only if sendEmail = true)
      if (sendEmail) {
        try {
          log.info("Attempting to send email notification for transaction status change from {} to {} for order {}", 
              oldTransactionStatus, transactionStatus, orderId);
          
          if ("SUCCESS".equals(transactionStatus) && "PENDING".equals(oldTransactionStatus)) {
            log.info("Sending payment success confirmation email for order {} with full product details", orderId);
            // Gửi email confirmation với đầy đủ thông tin sản phẩm khi thanh toán thành công
            emailService.sendOrderConfirmationEmail(orderDto);
            log.info("Payment success confirmation email sent successfully for order {}", orderId);
          } else if ("CANCELLED".equals(transactionStatus)) {
            log.info("Sending payment cancellation email for order {}", orderId);
            emailService.sendOrderCancellationEmail(orderDto);
            log.info("Payment cancellation email sent successfully for order {}", orderId);
          } else if (!transactionStatus.equals(oldTransactionStatus)) {
            log.info("Sending transaction status update email for order {} from {} to {}", 
                orderId, oldTransactionStatus, order.getOrderStatus());
            emailService.sendOrderStatusUpdateEmail(orderDto, oldOrderStatus, order.getOrderStatus());
            log.info("Transaction status update email sent successfully for order {}", orderId);
          }
        } catch (Exception e) {
          log.error("Error sending transaction status update email for order {}: {}", orderId, e.getMessage(), e);
          // Don't fail the transaction update if email fails, but log the error
        }
      } else {
        log.info("Skipping email notification for transaction status change (sendEmail=false)", orderId);
      }

      return orderDto;
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error updating transaction status for order {}: {}", orderId, e.getMessage(), e);
      throw new RuntimeException("Lỗi hệ thống khi cập nhật trạng thái thanh toán: " + e.getMessage());
    }
  }

  // Shop Order Management Methods
  public List<OrderDto> getShopOrdersWithFilters(String status, String paymentMethod, LocalDate startDate,
      LocalDate endDate) {
    // Get current shop ID from session/context
    UUID shopId = getCurrentShopId();
    if (shopId == null) {
      // Fallback: return empty list if no shop found
      System.out.println("DEBUG: No shop ID found for current user");
      return new ArrayList<>();
    }

    System.out.println("DEBUG: Found shop ID: " + shopId);
    System.out.println("DEBUG: Filters - status: " + status + ", paymentMethod: " + paymentMethod + ", startDate: "
        + startDate + ", endDate: " + endDate);

    List<Order> orders = orderRepository.findByShopIdWithFilters(shopId, status, paymentMethod, startDate, endDate);

    return orders.stream()
        .map(order -> {
          List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
          List<Transaction> transactions = transactionRepository.findByOrderId(order.getId());
          return convertToDto(order, orderItems, transactions);
        })
        .collect(Collectors.toList());
  }

  public OrderDto getShopOrderById(UUID orderId) {
    UUID shopId = getCurrentShopId();
    if (shopId == null) {
      throw new RuntimeException("Shop not found for current user");
    }

    Order order = orderRepository.findByIdAndShopId(orderId, shopId)
        .orElseThrow(() -> new RuntimeException("Order not found or does not belong to this shop"));

    List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
    List<Transaction> transactions = transactionRepository.findByOrderId(orderId);
    return convertToDto(order, orderItems, transactions);
  }

  @Transactional
  public OrderDto updateShopOrderStatus(UUID orderId, String status) {
    try {
      UUID shopId = getCurrentShopId();
      
      log.info("Shop {} attempting to update order {} status to {}", shopId, orderId, status);

      Order order = orderRepository.findByIdAndShopId(orderId, shopId)
          .orElseThrow(() -> {
            log.error("Order {} not found or does not belong to shop {}", orderId, shopId);
            return new RuntimeException("Không tìm thấy đơn hàng hoặc đơn hàng không thuộc về cửa hàng của bạn");
          });

      String oldStatus = order.getOrderStatus();
      log.info("Shop {} updating order {} status from {} to {}", shopId, orderId, oldStatus, status);
      
      order.setOrderStatus(status);

      if ("DELIVERED".equals(status)) {
        order.setDeliveredDate(OffsetDateTime.now());
        log.info("Order {} marked as delivered at {}", orderId, order.getDeliveredDate());
      } else if ("CANCELLED".equals(status)) {
        order.setCancelledDate(OffsetDateTime.now());
        log.info("Order {} marked as cancelled at {}", orderId, order.getCancelledDate());
        
        // Cập nhật trạng thái thanh toán thành CANCELLED khi hủy đơn hàng
        List<Transaction> transactions = transactionRepository.findByOrderId(orderId);
        for (Transaction transaction : transactions) {
          transaction.setTransactionStatus("CANCELLED");
          transaction.setTransactionDate(OffsetDateTime.now());
          transactionRepository.save(transaction);
          log.info("Transaction {} cancelled for order {}", transaction.getId(), orderId);
        }
      }

      order = orderRepository.save(order);
      log.info("Order {} status updated successfully to {}", orderId, status);

      List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
      List<Transaction> updatedTransactions = transactionRepository.findByOrderId(orderId);
      OrderDto orderDto = convertToDto(order, orderItems, updatedTransactions);

      // Send email notifications based on status change
      try {
        log.info("Attempting to send email notification for shop order {} status change from {} to {}", 
            orderId, oldStatus, status);
        
        if ("CANCELLED".equals(status)) {
          log.info("Sending cancellation email for order {}", orderId);
          emailService.sendOrderCancellationEmail(orderDto);
          log.info("Cancellation email sent successfully for order {}", orderId);
        } else if ("DELIVERED".equals(status)) {
          log.info("Sending delivery email for order {}", orderId);
          emailService.sendOrderDeliveryEmail(orderDto);
          log.info("Delivery email sent successfully for order {}", orderId);
        } else if (!oldStatus.equals(status)) {
          log.info("Sending status update email for order {} from {} to {}", orderId, oldStatus, status);
          emailService.sendOrderStatusUpdateEmail(orderDto, oldStatus, status);
          log.info("Status update email sent successfully for order {}", orderId);
        }
      } catch (Exception e) {
        log.error("Error sending order status update email for order {}: {}", orderId, e.getMessage(), e);
        // Don't fail the status update if email fails, but log the error
      }

      return orderDto;
    } catch (RuntimeException e) {
      // Re-throw RuntimeException để giữ nguyên message
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error updating shop order status for order {}: {}", orderId, e.getMessage(), e);
      throw new RuntimeException("Lỗi hệ thống khi cập nhật trạng thái đơn hàng: " + e.getMessage());
    }
  }

  public Map<String, Object> getShopOrderStatistics() {
    UUID shopId = getCurrentShopId();
    if (shopId == null) {
      // Return empty statistics if no shop found
      Map<String, Object> emptyStats = new HashMap<>();
      emptyStats.put("totalOrders", 0L);
      emptyStats.put("pendingOrders", 0L);
      emptyStats.put("pendingPaymentOrders", 0L);
      emptyStats.put("paidOrders", 0L);
      emptyStats.put("deliveredOrders", 0L);
      emptyStats.put("cancelledOrders", 0L);
      emptyStats.put("totalRevenue", BigDecimal.ZERO);
      return emptyStats;
    }

    Map<String, Object> statistics = new HashMap<>();

    // Đơn hàng theo trạng thái
    long pendingProcessingOrders = orderRepository.countByShopIdAndOrderStatus(shopId, "PENDING_PROCESSING");
    long processedOrders = orderRepository.countByShopIdAndOrderStatus(shopId, "PROCESSED");
    long readyForPickupOrders = orderRepository.countByShopIdAndOrderStatus(shopId, "READY_FOR_PICKUP");
    long inTransitOrders = orderRepository.countByShopIdAndOrderStatus(shopId, "IN_TRANSIT");
    long deliveredOrders = orderRepository.countByShopIdAndOrderStatus(shopId, "DELIVERED");
    long cancelledOrders = orderRepository.countByShopIdAndOrderStatus(shopId, "CANCELLED");

    // Tính tổng đơn hàng
    long totalOrders = pendingProcessingOrders + processedOrders + readyForPickupOrders +
        inTransitOrders + deliveredOrders + cancelledOrders;

    statistics.put("totalOrders", totalOrders);
    statistics.put("pendingProcessingOrders", pendingProcessingOrders);
    statistics.put("processedOrders", processedOrders);
    statistics.put("readyForPickupOrders", readyForPickupOrders);
    statistics.put("inTransitOrders", inTransitOrders);
    statistics.put("deliveredOrders", deliveredOrders);
    statistics.put("cancelledOrders", cancelledOrders);

    // Tổng doanh thu của shop
    BigDecimal totalRevenue = orderRepository.getTotalRevenueByShopId(shopId);
    statistics.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

    return statistics;
  }

  public Map<String, Object> getRevenueStatistics(String period, String startDate, String endDate) {
    System.out.println("DEBUG: getRevenueStatistics called with period=" + period + ", startDate=" + startDate
        + ", endDate=" + endDate);

    UUID shopId = getCurrentShopId();
    if (shopId == null) {
      System.out.println("DEBUG: No shop ID found");
      Map<String, Object> emptyStats = new HashMap<>();
      emptyStats.put("totalRevenue", BigDecimal.ZERO);
      emptyStats.put("totalOrders", 0L);
      emptyStats.put("averageOrderValue", BigDecimal.ZERO);
      emptyStats.put("revenueByPeriod", new ArrayList<>());
      emptyStats.put("topProducts", new ArrayList<>());
      return emptyStats;
    }

    System.out.println("DEBUG: Found shop ID: " + shopId);

    Map<String, Object> statistics = new HashMap<>();

    // Xử lý thời gian
    LocalDate startDateFilter = null;
    LocalDate endDateFilter = null;
    LocalDate today = LocalDate.now();

    if (startDate != null && !startDate.isEmpty() && !startDate.equals("null")) {
      try {
        startDateFilter = LocalDate.parse(startDate);
        System.out.println("DEBUG: Parsed startDate '" + startDate + "' to " + startDateFilter);
      } catch (Exception e) {
        System.err.println("Error parsing startDate: " + startDate);
      }
    }
    if (endDate != null && !endDate.isEmpty() && !endDate.equals("null")) {
      try {
        endDateFilter = LocalDate.parse(endDate);
        System.out.println("DEBUG: Parsed endDate '" + endDate + "' to " + endDateFilter);
      } catch (Exception e) {
        System.err.println("Error parsing endDate: " + endDate);
      }
    }

    System.out.println("DEBUG: After parsing dates - startDateFilter: " + startDateFilter + ", endDateFilter: " + endDateFilter);
    
    // Nếu không có startDate/endDate và không phải CUSTOM, sử dụng period
    if (startDateFilter == null && endDateFilter == null && period != null && !period.equals("ALL") && !period.equals("CUSTOM")) {
      switch (period) {
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
        case "THIS_YEAR":
          startDateFilter = today.withDayOfYear(1);
          endDateFilter = today;
          break;
      }
      System.out.println("DEBUG: Applied period logic - startDateFilter: " + startDateFilter + ", endDateFilter: " + endDateFilter);
    }

    // Nếu vẫn không có endDate, dùng hôm nay (chỉ khi không phải CUSTOM)
    if (endDateFilter == null && (period == null || !period.equals("CUSTOM"))) {
      endDateFilter = today;
    }
    
    // Kiểm tra nếu period là CUSTOM nhưng không có startDate hoặc endDate
    if ("CUSTOM".equals(period) && (startDateFilter == null || endDateFilter == null)) {
      System.out.println("DEBUG: CUSTOM period but missing startDate or endDate - using default period");
      // Fallback to THIS_MONTH if CUSTOM is selected but dates are missing
      startDateFilter = today.withDayOfMonth(1);
      endDateFilter = today;
    }
    
    // Nếu có startDate hoặc endDate nhưng period không phải CUSTOM, set period thành CUSTOM
    if ((startDateFilter != null || endDateFilter != null) && !"CUSTOM".equals(period)) {
      System.out.println("DEBUG: Found startDate or endDate but period is not CUSTOM - treating as CUSTOM");
    }

    System.out.println("DEBUG: Final date filters - startDateFilter: " + startDateFilter + ", endDateFilter: " + endDateFilter);
    System.out.println("DEBUG: Today is: " + today);
    System.out.println("DEBUG: Period: " + period);

        // Lấy doanh thu từ sản phẩm (không bao gồm tiền ship)
    BigDecimal totalRevenue = null;
    try {
      totalRevenue = transactionRepository.getTotalProductRevenueByShopIdAndStatus(
          shopId, "SUCCESS", startDateFilter, endDateFilter);
      System.out.println("DEBUG: Total product revenue (excluding shipping): " + totalRevenue);
    } catch (Exception e) {
      System.err.println("DEBUG: Error getting total product revenue: " + e.getMessage());
      totalRevenue = BigDecimal.ZERO;
    }
    
    // Lấy số đơn hàng đã thanh toán thành công
    long totalOrders = 0;
    try {
      totalOrders = transactionRepository.countSuccessfulOrdersByShopId(
          shopId, startDateFilter, endDateFilter);
      System.out.println("DEBUG: Total orders: " + totalOrders);
    } catch (Exception e) {
      System.err.println("DEBUG: Error getting total orders: " + e.getMessage());
    }

    // Tính giá trị đơn hàng trung bình
    BigDecimal averageOrderValue = totalOrders > 0
        ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
        : BigDecimal.ZERO;

    // Lấy doanh thu theo từng ngày/tuần/tháng (không bao gồm tiền ship)
    List<Object[]> revenueByPeriodRaw = null;
    try {
      revenueByPeriodRaw = transactionRepository.getProductRevenueByPeriod(
          shopId, "SUCCESS", startDateFilter, endDateFilter);
      System.out.println("DEBUG: Product revenue by period raw data size: "
          + (revenueByPeriodRaw != null ? revenueByPeriodRaw.size() : "null"));
    } catch (Exception e) {
      System.err.println("DEBUG: Error getting product revenue by period: " + e.getMessage());
      revenueByPeriodRaw = new ArrayList<>();
    }

    List<Map<String, Object>> revenueByPeriod = new ArrayList<>();
    if (revenueByPeriodRaw != null) {
      revenueByPeriod = revenueByPeriodRaw.stream()
          .map(row -> {
            Map<String, Object> periodData = new HashMap<>();
            try {
              System.out.println("DEBUG: Processing revenue period row - row[0]: "
                  + (row[0] != null ? row[0].getClass().getSimpleName() + ":" + row[0] : "null") +
                  ", row[1]: " + (row[1] != null ? row[1].getClass().getSimpleName() + ":" + row[1] : "null") +
                  ", row[2]: " + (row[2] != null ? row[2].getClass().getSimpleName() + ":" + row[2] : "null"));
              periodData.put("date", row[0]);
              periodData.put("revenue", row[1]);
              periodData.put("orderCount", row[2]);
              System.out.println(
                  "DEBUG: Revenue period data - date: " + row[0] + ", revenue: " + row[1] + ", orderCount: " + row[2]);
            } catch (Exception e) {
              System.err.println("DEBUG: Error processing revenue period data: " + e.getMessage());
              e.printStackTrace();
            }
            return periodData;
          })
          .collect(Collectors.toList());
    }

    // Lấy top sản phẩm bán chạy
    List<Object[]> topProductsRaw = null;
    try {
      topProductsRaw = orderItemRepository.getTopSellingProducts(
          shopId, startDateFilter, endDateFilter);
      System.out
          .println("DEBUG: Top products raw data size: " + (topProductsRaw != null ? topProductsRaw.size() : "null"));
    } catch (Exception e) {
      System.err.println("DEBUG: Error getting top products: " + e.getMessage());
      topProductsRaw = new ArrayList<>();
    }

    List<Map<String, Object>> topProducts = new ArrayList<>();
    if (topProductsRaw != null) {
      topProducts = topProductsRaw.stream()
          .map(row -> {
            Map<String, Object> productData = new HashMap<>();
            try {
              System.out.println("DEBUG: Processing top product row - row[0]: "
                  + (row[0] != null ? row[0].getClass().getSimpleName() + ":" + row[0] : "null") +
                  ", row[1]: " + (row[1] != null ? row[1].getClass().getSimpleName() + ":" + row[1] : "null") +
                  ", row[2]: " + (row[2] != null ? row[2].getClass().getSimpleName() + ":" + row[2] : "null") +
                  ", row[3]: " + (row[3] != null ? row[3].getClass().getSimpleName() + ":" + row[3] : "null") +
                  ", row[4]: " + (row[4] != null ? row[4].getClass().getSimpleName() + ":" + row[4] : "null") +
                  ", row[5]: " + (row[5] != null ? row[5].getClass().getSimpleName() + ":" + row[5] : "null"));
              productData.put("productName", row[0]);
              productData.put("variantName", row[1]);
              productData.put("variantValue", row[2]);
              productData.put("totalQuantity", row[3]);
              productData.put("totalRevenue", row[4]);
              productData.put("orderCount", row[5]);
              System.out.println("DEBUG: Top product data - name: " + row[0] + ", variant: " + row[1] + ", value: "
                  + row[2] + ", quantity: " + row[3] + ", revenue: " + row[4] + ", orderCount: " + row[5]);
            } catch (Exception e) {
              System.err.println("DEBUG: Error processing top product data: " + e.getMessage());
              e.printStackTrace();
            }
            return productData;
          })
          .collect(Collectors.toList());
    }

    statistics.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
    statistics.put("totalOrders", totalOrders);
    statistics.put("averageOrderValue", averageOrderValue);
    statistics.put("revenueByPeriod", revenueByPeriod);
    statistics.put("topProducts", topProducts);
    statistics.put("period", period);
    statistics.put("startDate", startDateFilter);
    statistics.put("endDate", endDateFilter);

    return statistics;
  }

  // Helper method to get current shop ID (implement based on your authentication)
  private UUID getCurrentShopId() {
    try {
      // Get current user from session
      Account currentUser = getCurrentUser();
      if (currentUser == null) {
        log.error("No current user found in session");
        throw new RuntimeException("Bạn chưa đăng nhập hoặc phiên đăng nhập đã hết hạn");
      }

      log.info("Current user: {}", currentUser.getUsername());

      // Get shop for current user
      Shop shop = shopRepository.findByUser(currentUser);
      if (shop == null) {
        log.error("No shop found for user: {}", currentUser.getUsername());
        throw new RuntimeException("Bạn chưa đăng ký cửa hàng. Vui lòng đăng ký cửa hàng trước khi quản lý đơn hàng");
      }

      if (!shop.getIsActive()) {
        log.error("Shop is inactive for user: {}", currentUser.getUsername());
        throw new RuntimeException("Cửa hàng của bạn đã bị khóa. Vui lòng liên hệ admin để được hỗ trợ");
      }

      log.info("Found active shop: {} (ID: {})", shop.getShopName(), shop.getId());
      return shop.getId();
    } catch (RuntimeException e) {
      // Re-throw RuntimeException để giữ nguyên message
      throw e;
    } catch (Exception e) {
      log.error("Error getting current shop ID: {}", e.getMessage(), e);
      throw new RuntimeException("Lỗi hệ thống khi xác định cửa hàng: " + e.getMessage());
    }
  }

  // Helper method to get current user (implement based on your authentication)
  private Account getCurrentUser() {
    try {
      // Get current user from session using AccountService
      // This is a simplified implementation - you may need to adjust based on your
      // auth system
      return accountService.getCurrentUserFromSession();
    } catch (Exception e) {
      throw new RuntimeException("Error getting current user: " + e.getMessage());
    }
  }

  private OrderDto convertToDto(Order order, List<OrderItem> orderItems, List<Transaction> transactions) {
    OrderDto dto = new OrderDto();
    dto.setId(order.getId());

    // Account info
    if (order.getAccount() != null) {
      dto.setAccountId(order.getAccount().getId());
      dto.setAccountName(
          order.getAccount().getName() != null ? order.getAccount().getName() : order.getAccount().getUsername());
      dto.setAccountEmail(order.getAccount().getEmail());
      dto.setAccountPhone(order.getAccount().getPhone());
    } else {
      dto.setAccountId(null);
      dto.setAccountName("Khách hàng không xác định");
      dto.setAccountEmail("N/A");
      dto.setAccountPhone("N/A");
    }

    // Shop info
    if (order.getShop() != null) {
      dto.setShopId(order.getShop().getId());
      dto.setShopName(order.getShop().getShopName());
    } else {
      dto.setShopId(null);
      dto.setShopName("Cửa hàng không xác định");
    }

    // Shipping info
    if (order.getShipping() != null) {
      dto.setShippingId(order.getShipping().getId());
      dto.setShippingMethod(order.getShipping().getShippingMethod());
      dto.setShippingPrice(order.getShipping().getPrice());
    } else {
      dto.setShippingId(null);
      dto.setShippingMethod("Phương thức vận chuyển không xác định");
      dto.setShippingPrice(BigDecimal.ZERO);
    }

    // Payment info
    if (order.getPayment() != null) {
      dto.setPaymentId(order.getPayment().getId());
      dto.setPaymentName(order.getPayment().getPaymentName());
    } else {
      dto.setPaymentId(null);
      dto.setPaymentName("Phương thức thanh toán không xác định");
    }

    dto.setTotalAmount(order.getTotalAmount());
    dto.setDiscountAmount(order.getDiscountAmount());
    dto.setOrderStatus(order.getOrderStatus());
    dto.setOrderCode(order.getOrderCode());
    dto.setOrderDate(order.getOrderDate());
    dto.setDeliveredDate(order.getDeliveredDate());
    dto.setCancelledDate(order.getCancelledDate());
    dto.setShippingAddress(order.getShippingAddress());

    // Log để debug total amount
    log.info("Order {} - TotalAmount: {}, DiscountAmount: {}, ShippingPrice: {}", 
        order.getId(), order.getTotalAmount(), order.getDiscountAmount(), 
        order.getShipping() != null ? order.getShipping().getPrice() : "N/A");

    // Set transaction status (lấy từ transaction đầu tiên hoặc PENDING nếu không
    // có)
    if (!transactions.isEmpty()) {
      dto.setTransactionStatus(transactions.get(0).getTransactionStatus());
    } else {
      dto.setTransactionStatus("PENDING");
    }

    // Convert order items
    dto.setOrderItems(orderItems.stream()
        .map(item -> {
          OrderDto.OrderItemDto itemDto = new OrderDto.OrderItemDto();
          itemDto.setId(item.getId());

          if (item.getProductVariant() != null) {
            itemDto.setProductVariantId(item.getProductVariant().getId());
            itemDto.setProductId(item.getProductVariant().getProduct().getId()); // Add product ID
            itemDto.setProductName(item.getProductVariant().getProduct().getName());
            // Set product image with fallback
            String productImage = item.getProductVariant().getProduct().getProductImage();
            itemDto
                .setProductImage(productImage != null && !productImage.isEmpty() ? productImage : "/placeholder.png");
            
            // Set variant information with validation
            String variantName = item.getProductVariant().getVariantName();
            String variantValue = item.getProductVariant().getVariantValue();
            
            // Log để debug
            System.out.println("Variant info for order item: " + item.getId());
            System.out.println("  - VariantName: " + variantName);
            System.out.println("  - VariantValue: " + variantValue);
            
            itemDto.setVariantName(variantName != null ? variantName : "");
            itemDto.setVariantValue(variantValue != null ? variantValue : "");
          } else {
            // For products without variants, set default values
            itemDto.setProductVariantId(null);
            itemDto.setProductId(null);
            itemDto.setProductName("Sản phẩm không xác định");
            itemDto.setProductImage("/placeholder.png");
            itemDto.setVariantName("");
            itemDto.setVariantValue("");
          }

          itemDto.setQuantity(item.getQuantity());
          itemDto.setProductPrice(item.getProductPrice());
          itemDto.setDiscountApplied(item.getDiscountApplied());
          return itemDto;
        })
        .collect(Collectors.toList()));

    // Convert transactions
    dto.setTransactions(transactions.stream()
        .map(transaction -> {
          OrderDto.TransactionDto transactionDto = new OrderDto.TransactionDto();
          transactionDto.setId(transaction.getId());
          transactionDto.setTransactionCode(transaction.getTransactionCode());
          transactionDto.setTransactionAmount(transaction.getTransactionAmount());
          transactionDto.setTransactionStatus(transaction.getTransactionStatus());
          transactionDto.setTransactionDate(transaction.getTransactionDate());
          transactionDto.setVnpayResponseCode(transaction.getVnpayResponseCode());
          return transactionDto;
        })
        .collect(Collectors.toList()));

    return dto;
  }

  /**
   * Trừ số lượng sản phẩm khi đơn hàng được thanh toán thành công
   */
  @Transactional
  private void deductProductQuantities(UUID orderId) {
    try {
      List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

      for (OrderItem orderItem : orderItems) {
        if (orderItem.getProductVariant() != null) {
          ProductVariant productVariant = orderItem.getProductVariant();
          Integer currentQuantity = productVariant.getQuantity();
          Integer orderedQuantity = orderItem.getQuantity();

          if (currentQuantity != null && orderedQuantity != null) {
            // Kiểm tra xem có đủ số lượng không
            if (currentQuantity >= orderedQuantity) {
              // Trừ số lượng
              int newQuantity = currentQuantity - orderedQuantity;
              productVariant.setQuantity(newQuantity);
              productVariantRepository.save(productVariant);

              System.out.println("Đã trừ " + orderedQuantity + " sản phẩm từ variant " +
                  productVariant.getId() + ". Số lượng còn lại: " + newQuantity);
            } else {
              System.err.println("Không đủ số lượng sản phẩm variant " + productVariant.getId() +
                  ". Cần: " + orderedQuantity + ", Có: " + currentQuantity);
            }
          }
        }
      }
    } catch (Exception e) {
      System.err.println("Lỗi khi trừ số lượng sản phẩm cho đơn hàng " + orderId + ": " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Hoàn trả số lượng sản phẩm khi hủy đơn hàng
   */
  @Transactional
  private void restoreProductQuantities(UUID orderId) {
    try {
      List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

      for (OrderItem orderItem : orderItems) {
        if (orderItem.getProductVariant() != null) {
          ProductVariant productVariant = orderItem.getProductVariant();
          Integer currentQuantity = productVariant.getQuantity();
          Integer orderedQuantity = orderItem.getQuantity();

          if (currentQuantity != null && orderedQuantity != null) {
            // Hoàn trả số lượng
            int newQuantity = currentQuantity + orderedQuantity;
            productVariant.setQuantity(newQuantity);
            productVariantRepository.save(productVariant);

            System.out.println("Đã hoàn trả " + orderedQuantity + " sản phẩm cho variant " +
                productVariant.getId() + ". Số lượng hiện tại: " + newQuantity);
          }
        }
      }
    } catch (Exception e) {
      System.err.println("Lỗi khi hoàn trả số lượng sản phẩm cho đơn hàng " + orderId + ": " + e.getMessage());
      e.printStackTrace();
    }
  }

  public OrderDto createSampleOrder(UUID accountId) {
    // Tạo đơn hàng mẫu cho testing
    Account account = accountRepository.findById(accountId)
        .orElseThrow(() -> new RuntimeException("Account not found"));

    // Tạo shop mẫu nếu chưa có
    Shop shop = shopRepository.findAll().stream().findFirst()
        .orElseGet(() -> {
          Shop demoShop = new Shop();
          demoShop.setShopName("Shop Demo");
          demoShop.setDescription("Shop demo cho testing");
          demoShop.setPhone("0123456789");
          demoShop.setEmail("demo@shop.com");
          demoShop.setAddress("123 Đường Demo, Quận 1, TP.HCM");
          demoShop.setCreatedAt(OffsetDateTime.now());
          demoShop.setIsActive(true);
          return shopRepository.save(demoShop);
        });

    // Tạo shipping mẫu
    Shipping shipping = shippingRepository.findAll().stream().findFirst()
        .orElseGet(() -> {
          Shipping demoShipping = new Shipping();
          demoShipping.setShippingMethod("Giao hàng tiêu chuẩn");
          demoShipping.setDescription("Giao hàng trong 2-3 ngày");
          demoShipping.setPrice(new BigDecimal("30000"));
          demoShipping.setEstimatedDelivery("2-3 ngày");
          demoShipping.setIsActive(true);
          return shippingRepository.save(demoShipping);
        });

    // Tạo payment mẫu
    Payment payment = paymentRepository.findAll().stream().findFirst()
        .orElseGet(() -> {
          Payment demoPayment = new Payment();
          demoPayment.setPaymentCode("COD");
          demoPayment.setPaymentType("CASH");
          demoPayment.setPaymentName("Thanh toán khi nhận hàng");
          demoPayment.setDescription("Thanh toán tiền mặt khi nhận hàng");
          demoPayment.setIsActive(true);
          return paymentRepository.save(demoPayment);
        });

    // Tạo order
    Order order = new Order();
    order.setAccount(account);
    order.setShop(shop);
    order.setShipping(shipping);
    order.setPayment(payment);
    order.setTotalAmount(new BigDecimal("150000"));
    order.setDiscountAmount(new BigDecimal("10000"));
    order.setOrderStatus("PENDING_PROCESSING");
    order.setOrderDate(OffsetDateTime.now());
    order.setShippingAddress("456 Đường Test, Quận 2, TP.HCM");
    order.setOrderCode("ORDER" + System.currentTimeMillis());

    Order savedOrder = orderRepository.save(order);

    // Tạo order items mẫu
    List<OrderItem> orderItems = new ArrayList<>();
    
    // Order item 1
    OrderItem item1 = new OrderItem();
    item1.setOrder(savedOrder);
    item1.setQuantity(2);
    item1.setProductPrice(new BigDecimal("50000"));
    item1.setDiscountApplied(new BigDecimal("5000"));
    orderItems.add(orderItemRepository.save(item1));

    // Order item 2
    OrderItem item2 = new OrderItem();
    item2.setOrder(savedOrder);
    item2.setQuantity(1);
    item2.setProductPrice(new BigDecimal("80000"));
    item2.setDiscountApplied(new BigDecimal("5000"));
    orderItems.add(orderItemRepository.save(item2));

    // Tạo transaction mẫu
    Transaction transaction = new Transaction();
    transaction.setOrder(savedOrder);
    transaction.setPayment(payment);
    transaction.setTransactionCode("TXN" + System.currentTimeMillis());
    transaction.setTransactionAmount(new BigDecimal("140000"));
    transaction.setTransactionStatus("PENDING");
    transaction.setTransactionDate(OffsetDateTime.now());
    transactionRepository.save(transaction);

    return convertToDto(savedOrder, orderItems, List.of(transaction));
  }

  public OrderDto cancelOrder(UUID orderId) {
    try {
      log.info("Cancelling order: {}", orderId);

      // Tìm đơn hàng
      Order order = orderRepository.findById(orderId)
          .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

      // Kiểm tra trạng thái đơn hàng
      if ("CANCELLED".equals(order.getOrderStatus())) {
        throw new RuntimeException("Đơn hàng đã bị hủy trước đó");
      }

      if ("DELIVERED".equals(order.getOrderStatus())) {
        throw new RuntimeException("Không thể hủy đơn hàng đã giao");
      }

      // Cập nhật trạng thái đơn hàng
      order.setOrderStatus("CANCELLED");
      order.setCancelledDate(OffsetDateTime.now());
      Order savedOrder = orderRepository.save(order);

      // Cập nhật trạng thái transaction
      List<Transaction> transactions = transactionRepository.findByOrderId(orderId);
      for (Transaction transaction : transactions) {
        if ("PENDING".equals(transaction.getTransactionStatus()) || "SUCCESS".equals(transaction.getTransactionStatus())) {
          transaction.setTransactionStatus("CANCELLED");
          transactionRepository.save(transaction);
        }
      }

      // Hoàn lại số lượng sản phẩm cho cả COD và PayOS khi hủy đơn hàng
      // Với COD: số lượng đã được trừ khi tạo đơn hàng
      // Với PayOS: số lượng đã được trừ khi thanh toán thành công
      restoreProductQuantities(orderId);

      // Lấy order items cho DTO
      List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

      // Gửi email thông báo hủy đơn hàng
      try {
        log.info("Attempting to send cancellation email for order {}", orderId);
        OrderDto orderDto = convertToDto(savedOrder, orderItems, transactions);
        emailService.sendOrderCancellationEmail(orderDto);
        log.info("Cancellation email sent successfully for order {}", orderId);
      } catch (Exception e) {
        log.error("Error sending cancellation email for order {}: {}", orderId, e.getMessage(), e);
      }

      return convertToDto(savedOrder, orderItems, transactions);

    } catch (Exception e) {
      log.error("Error cancelling order {}: ", orderId, e);
      throw new RuntimeException("Lỗi hủy đơn hàng: " + e.getMessage());
    }
  }

  @Transactional
  public void deleteOrder(UUID orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new RuntimeException("Order not found"));

    log.info("Deleting order: {}", orderId);

    // Xóa order items trước
    List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
    for (OrderItem item : orderItems) {
      orderItemRepository.delete(item);
    }

    // Xóa transactions
    List<Transaction> transactions = transactionRepository.findByOrderId(orderId);
    for (Transaction transaction : transactions) {
      transactionRepository.delete(transaction);
    }

    // Xóa order
    orderRepository.delete(order);

    log.info("Order {} deleted successfully", orderId);
  }
}