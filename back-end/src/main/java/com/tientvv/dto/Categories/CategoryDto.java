package com.tientvv.dto.Categories;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryDto {
  private UUID id;
  private String name;
  private String categoryImage;
}
