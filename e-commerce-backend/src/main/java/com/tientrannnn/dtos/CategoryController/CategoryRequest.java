package com.tientrannnn.dtos.CategoryController;

import lombok.Data;

import java.util.UUID;

@Data
public class CategoryRequest {
  private String name;
  private String categoryImage;
  private UUID parentId;

  @Override
  public String toString() {
    return "CategoryRequest{" +
        "name='" + name + '\'' +
        ", categoryImage='" + categoryImage + '\'' +
        ", parentId=" + parentId +
        '}';
  }
}
