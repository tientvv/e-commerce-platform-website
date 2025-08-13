package com.tientvv.dto.CrudProduct;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;

@Getter
public class UpdateProductDto {
  private UUID id;
  private String name;
  private String brand;
  private String description;
  private UUID categoryId;
  private MultipartFile productImage;

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
  public void setId(UUID id) {
    System.out.println("UpdateProductDto.setId called with: " + id);
    this.id = id;
  }

  public void setName(String name) {
    System.out.println("UpdateProductDto.setName called with: " + name);
    this.name = name;
  }

  public void setBrand(String brand) {
    System.out.println("UpdateProductDto.setBrand called with: " + brand);
    this.brand = brand;
  }

  public void setDescription(String description) {
    System.out.println("UpdateProductDto.setDescription called with: " + description);
    this.description = description;
  }

  public void setProductImage(MultipartFile productImage) {
    System.out.println("UpdateProductDto.setProductImage called with: " + (productImage != null ? productImage.getOriginalFilename() : "null"));
    this.productImage = productImage;
  }

  // Debug method
  @Override
  public String toString() {
    return "UpdateProductDto{" +
        "id=" + id +
        ", categoryId=" + categoryId +
        ", brand='" + brand + '\'' +
        ", name='" + name + '\'' +
        ", description='" + description + '\'' +
        ", hasImage=" + (productImage != null) +
        '}';
  }
}
