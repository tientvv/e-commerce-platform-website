package com.tientrannnn.dtos.CategoryController;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class CategoryResponse {
  private UUID id;
  private String name;
  private String categoryImage;
  private UUID parentId;
  private String parentName;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;
  private List<CategoryResponse> children;
}
