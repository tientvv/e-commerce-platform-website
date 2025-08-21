package com.tientvv.dto.CrudProduct;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductImageDto {
  private UUID productId;
  private UUID productVariantId;
}