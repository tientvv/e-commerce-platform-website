package com.tientvv.dto.CrudProduct;

import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateProductVariantDto {
  private UUID productId;
  private String variantName;
  private String variantValue;
  private Integer quantity;
  private BigDecimal price;
  private Boolean isActive;
}