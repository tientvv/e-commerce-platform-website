package com.tientvv.dto.shipping;

import com.tientvv.model.Shipping;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShippingDto {
  private UUID id;
  private String shippingMethod;
  private BigDecimal price;
  private String estimatedDelivery;
  private Boolean isActive;

  public static ShippingDto fromEntity(Shipping shipping) {
    return new ShippingDto(
        shipping.getId(),
        shipping.getShippingMethod(),
        shipping.getPrice(),
        shipping.getEstimatedDelivery(),
        shipping.getIsActive());
  }
}