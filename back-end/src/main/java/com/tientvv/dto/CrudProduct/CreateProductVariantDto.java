package com.tientvv.dto.CrudProduct;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductVariantDto {
  private UUID productId;
  private String variantName;
  private String variantValue;
  private Integer quantity;
  private BigDecimal price;
}