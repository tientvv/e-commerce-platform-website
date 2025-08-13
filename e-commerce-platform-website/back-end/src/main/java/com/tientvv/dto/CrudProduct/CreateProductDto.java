package com.tientvv.dto.CrudProduct;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;
import lombok.Getter;

@Getter
public class CreateProductDto {
  private UUID categoryId;
  private String brand;
  private String name;
  private MultipartFile productImage;
  private String description;
  private UUID shopId;

  // Custom setter để xử lý categoryId từ string
  public void setCategoryId(String categoryId) {
    if (categoryId != null && !categoryId.trim().isEmpty()) {
      try {
        this.categoryId = UUID.fromString(categoryId.trim());
      } catch (IllegalArgumentException e) {
        this.categoryId = null;
      }
    } else {
      this.categoryId = null;
    }
  }

  // Manual setters for other fields
  public void setBrand(String brand) {
    this.brand = brand;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setProductImage(MultipartFile productImage) {
    this.productImage = productImage;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setShopId(UUID shopId) {
    this.shopId = shopId;
  }

  // Debug method
  @Override
  public String toString() {
    return "CreateProductDto{" +
        "categoryId=" + categoryId +
        ", brand='" + brand + '\'' +
        ", name='" + name + '\'' +
        ", description='" + description + '\'' +
        ", shopId=" + shopId +
        ", hasImage=" + (productImage != null) +
        '}';
  }
}
