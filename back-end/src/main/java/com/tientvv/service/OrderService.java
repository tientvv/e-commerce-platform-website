package com.tientvv.service;

import com.tientvv.dto.order.CreateOrderDto;
import com.tientvv.dto.order.OrderDto;
import com.tientvv.model.*;
import com.tientvv.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
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

  @SuppressWarnings("unused")
  @Autowired
  private PayOSService payOSService;

  @Autowired
  private AccountService accountService;

  @SuppressWarnings("unused")
  @Autowired
  private DiscountRepository discountRepository;

  @Transactional
  public OrderDto createOrder(CreateOrderDto createOrderDto) {
    // Validate entities exist
    Account account = accountRepository.findById(createOrderDto.getAccountId())
        .orElseThrow(() -> new RuntimeException("Account not found"));

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
      order.setOrderStatus("PENDING_PAYMENT");
    } else {
      order.setOrderStatus("PENDING");
    }

    order.setOrderDate(OffsetDateTime.now());
    order.setShippingAddress(createOrderDto.getShippingAddress());

    // Save order
    Order savedOrder = orderRepository.save(order);

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

    return convertToDto(savedOrder, orderItems, transactions);
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

  @Transactional
  public OrderDto updateOrderStatus(UUID orderId, String status) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new RuntimeException("Order not found"));

    order.setOrderStatus(status);

    if ("DELIVERED".equals(status)) {
      order.setDeliveredDate(OffsetDateTime.now());
    } else if ("CANCELLED".equals(status)) {
      order.setCancelledDate(OffsetDateTime.now());
    }

    order = orderRepository.save(order);

    List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
    List<Transaction> transactions = transactionRepository.findByOrderId(orderId);

    return convertToDto(order, orderItems, transactions);
  }

  @Transactional
  public Transaction updateTransactionStatus(UUID transactionId, String status, String bankResponseCode) {
    Transaction transaction = transactionRepository.findById(transactionId)
        .orElseThrow(() -> new RuntimeException("Transaction not found"));

    transaction.setTransactionStatus(status);
    if (bankResponseCode != null) {
      transaction.setVnpayResponseCode(bankResponseCode);
    }
    transaction.setTransactionDate(OffsetDateTime.now());

    return transactionRepository.save(transaction);
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
    UUID shopId = getCurrentShopId();
    if (shopId == null) {
      throw new RuntimeException("Shop not found for current user");
    }

    Order order = orderRepository.findByIdAndShopId(orderId, shopId)
        .orElseThrow(() -> new RuntimeException("Order not found or does not belong to this shop"));

    order.setOrderStatus(status);
    orderRepository.save(order);

    List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
    List<Transaction> transactions = transactionRepository.findByOrderId(orderId);
    return convertToDto(order, orderItems, transactions);
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

    // Tổng số đơn hàng của shop
    long totalOrders = orderRepository.countByShopId(shopId);
    statistics.put("totalOrders", totalOrders);

    // Đơn hàng theo trạng thái
    long pendingOrders = orderRepository.countByShopIdAndOrderStatus(shopId, "PENDING");
    long pendingPaymentOrders = orderRepository.countByShopIdAndOrderStatus(shopId, "PENDING_PAYMENT");
    long paidOrders = orderRepository.countByShopIdAndOrderStatus(shopId, "PAID");
    long deliveredOrders = orderRepository.countByShopIdAndOrderStatus(shopId, "DELIVERED");
    long cancelledOrders = orderRepository.countByShopIdAndOrderStatus(shopId, "CANCELLED");

    statistics.put("pendingOrders", pendingOrders);
    statistics.put("pendingPaymentOrders", pendingPaymentOrders);
    statistics.put("paidOrders", paidOrders);
    statistics.put("deliveredOrders", deliveredOrders);
    statistics.put("cancelledOrders", cancelledOrders);

    // Tổng doanh thu của shop
    BigDecimal totalRevenue = orderRepository.getTotalRevenueByShopId(shopId);
    statistics.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

    return statistics;
  }

  // Helper method to get current shop ID (implement based on your authentication)
  private UUID getCurrentShopId() {
    try {
      // Get current user from session
      Account currentUser = getCurrentUser();
      if (currentUser == null) {
        System.out.println("No current user found");
        return null;
      }

      System.out.println("Current user: " + currentUser.getUsername());

      // Get shop for current user
      Shop shop = shopRepository.findByUser(currentUser);
      if (shop == null) {
        System.out.println("No shop found for user: " + currentUser.getUsername());
        return null;
      }

      System.out.println("Found shop: " + shop.getShopName() + " (ID: " + shop.getId() + ")");
      return shop.getId();
    } catch (Exception e) {
      System.err.println("Error getting current shop ID: " + e.getMessage());
      e.printStackTrace();
      return null;
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
    dto.setOrderDate(order.getOrderDate());
    dto.setDeliveredDate(order.getDeliveredDate());
    dto.setCancelledDate(order.getCancelledDate());
    dto.setShippingAddress(order.getShippingAddress());

    // Convert order items
    dto.setOrderItems(orderItems.stream()
        .map(item -> {
          OrderDto.OrderItemDto itemDto = new OrderDto.OrderItemDto();
          itemDto.setId(item.getId());

          if (item.getProductVariant() != null) {
            itemDto.setProductVariantId(item.getProductVariant().getId());
            itemDto.setProductName(item.getProductVariant().getProduct().getName());
            // Set product image with fallback
            String productImage = item.getProductVariant().getProduct().getProductImage();
            itemDto
                .setProductImage(productImage != null && !productImage.isEmpty() ? productImage : "/placeholder.png");
            itemDto.setVariantName(item.getProductVariant().getVariantName());
            itemDto.setVariantValue(item.getProductVariant().getVariantValue());
          } else {
            // For products without variants, set default values
            itemDto.setProductVariantId(null);
            itemDto.setProductName("Sản phẩm không xác định");
            itemDto.setProductImage("/placeholder.png");
            itemDto.setVariantName(null);
            itemDto.setVariantValue(null);
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
}