package com.tientvv.dto.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.Map;

@Data
public class CreateOrderDto {
  @NotNull(message = "Account ID không được để trống")
  private UUID accountId;

  @NotNull(message = "Shop ID không được để trống")
  private UUID shopId;

  @NotNull(message = "Shipping ID không được để trống")
  private UUID shippingId;

  @NotNull(message = "Payment ID không được để trống")
  private UUID paymentId;

  @NotNull(message = "Tổng tiền không được để trống")
  private BigDecimal totalAmount;

  private BigDecimal discountAmount;

  @Size(max = 50, message = "Trạng thái đơn hàng không được quá 50 ký tự")
  private String orderStatus;

  @Size(max = 500, message = "Địa chỉ giao hàng không được quá 500 ký tự")
  private String shippingAddress;

  @NotNull(message = "Danh sách sản phẩm không được để trống")
  private List<OrderItemDto> orderItems;

  private String discountCode;

  private Map<String, Object> additionalData;

  @Data
  public static class OrderItemDto {
    private UUID productVariantId; // Can be null for products without variants

    @NotNull(message = "Số lượng không được để trống")
    private Integer quantity;

    @NotNull(message = "Giá sản phẩm không được để trống")
    private BigDecimal productPrice;

    private BigDecimal discountApplied;
  }
}