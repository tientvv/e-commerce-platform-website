package com.tientvv.dto.CrudProduct;

import java.math.BigDecimal;
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

  String getShopName();

  UUID getShopId();

  BigDecimal getMinPrice();

  BigDecimal getMaxPrice();

  BigDecimal getOriginalPrice();

  Integer getDiscountPercentage();

  Integer getViewCount();

  Integer getSoldCount();

  Double getRating();

  Integer getReviewCount();
}