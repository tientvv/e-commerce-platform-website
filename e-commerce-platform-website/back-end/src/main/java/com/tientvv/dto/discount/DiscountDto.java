package com.tientvv.dto.discount;

import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class DiscountDto {
  private UUID id;
  private String name;
  private String description;
  private String discountType; // PERCENTAGE, FIXED_AMOUNT
  private BigDecimal discountValue;
  private OffsetDateTime startDate;
  private OffsetDateTime endDate;
  private BigDecimal minOrderValue;
  private String applicationType; // ALL, PRODUCT, CATEGORY, VARIANT
  private UUID productId;
  private String productName;
  private UUID categoryId;
  private String categoryName;
  private UUID productVariantId;
  private String productVariantName;
  private Boolean isActive;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;
}