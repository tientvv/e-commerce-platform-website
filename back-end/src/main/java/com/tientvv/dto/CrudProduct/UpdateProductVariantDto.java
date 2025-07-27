package com.tientvv.dto.CrudProduct;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductVariantDto {
  private String variantName;
  private String variantValue;
  private Integer quantity;
  private BigDecimal price;
  private Boolean isActive;
}