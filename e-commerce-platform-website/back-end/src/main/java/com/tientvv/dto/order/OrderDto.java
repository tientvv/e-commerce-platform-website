package com.tientvv.dto.order;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class OrderDto {
  private UUID id;
  private UUID accountId;
  private String accountName;
  private String accountEmail;
  private String accountPhone;
  private UUID shopId;
  private String shopName;
  private UUID shippingId;
  private String shippingMethod;
  private BigDecimal shippingPrice;
  private UUID paymentId;
  private String paymentName;
  private BigDecimal totalAmount;
  private BigDecimal discountAmount;
  private String orderStatus;
  private String orderCode;
  private OffsetDateTime orderDate;
  private OffsetDateTime deliveredDate;
  private OffsetDateTime cancelledDate;
  private String shippingAddress;
  private String transactionStatus; // Thêm field này để frontend dễ truy cập
  private List<OrderItemDto> orderItems;
  private List<TransactionDto> transactions;

  @Data
  public static class OrderItemDto {
    private UUID id;
    private UUID productVariantId;
    private String productName;
    private String productImage;
    private String variantName;
    private String variantValue;
    private Integer quantity;
    private BigDecimal productPrice;
    private BigDecimal discountApplied;
  }

  @Data
  public static class TransactionDto {
    private UUID id;
    private String transactionCode;
    private BigDecimal transactionAmount;
    private String transactionStatus;
    private OffsetDateTime transactionDate;
    private String vnpayResponseCode;
  }
}