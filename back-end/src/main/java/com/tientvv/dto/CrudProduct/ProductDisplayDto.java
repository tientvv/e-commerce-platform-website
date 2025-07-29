package com.tientvv.dto.CrudProduct;

import java.math.BigDecimal;
import java.util.UUID;

public interface ProductDisplayDto {
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

  String getShopName();

  Integer getViewCount();

  Integer getSoldCount();

  Double getRating();

  Integer getReviewCount();
}