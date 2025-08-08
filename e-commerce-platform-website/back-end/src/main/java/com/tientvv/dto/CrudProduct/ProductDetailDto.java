package com.tientvv.dto.CrudProduct;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface ProductDetailDto {
  UUID getId();

  String getName();

  String getBrand();

  String getDescription();

  String getProductImage();

  Boolean getIsActive();

  String getCategoryName();

  UUID getCategoryId();

  BigDecimal getMinPrice();

  BigDecimal getMaxPrice();

  BigDecimal getOriginalPrice();

  Integer getDiscountPercentage();

  Integer getDiscountAmount();

  String getDiscountType();

  String getDiscountName();

  OffsetDateTime getDiscountStartDate();

  OffsetDateTime getDiscountEndDate();

  BigDecimal getMinOrderValue();

  String getShopName();

  UUID getShopId();

  Integer getViewCount();

  Integer getSoldCount();

  Double getRating();

  Integer getReviewCount();
}