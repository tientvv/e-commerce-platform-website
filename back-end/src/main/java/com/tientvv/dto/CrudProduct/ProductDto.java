package com.tientvv.dto.CrudProduct;

import java.util.UUID;
import com.tientvv.model.Product;

public interface ProductDto {

  UUID getId();

  String getName();

  String getBrand();

  String getDescription();

  String getProductImage();

  Boolean getIsActive();

  String getCategoryName();

  UUID getCategoryId();

  static ProductDto fromEntity(Product product) {
    return new ProductDto() {
      @Override
      public UUID getId() {
        return product.getId();
      }

      @Override
      public String getName() {
        return product.getName();
      }

      @Override
      public String getBrand() {
        return product.getBrand();
      }

      @Override
      public String getDescription() {
        return product.getDescription();
      }

      @Override
      public String getProductImage() {
        return product.getProductImage();
      }

      @Override
      public Boolean getIsActive() {
        return product.getIsActive();
      }

      @Override
      public String getCategoryName() {
        return product.getCategory() != null ? product.getCategory().getName() : null;
      }

      @Override
      public UUID getCategoryId() {
        return product.getCategory() != null ? product.getCategory().getId() : null;
      }
    };
  }
}