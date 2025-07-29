package com.tientvv.dto.Categories;

import org.springframework.web.multipart.MultipartFile;
import lombok.Data;

@Data
public class CreateCategoryDto {
  private String name;
  private MultipartFile categoryImage;
}