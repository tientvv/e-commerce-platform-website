package com.tientvv.dto.Categories;

import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;
import lombok.Data;

@Data
public class UpdateCategoryDto {
  private UUID id;
  private String name;
  private MultipartFile categoryImage;
  private String existingImageUrl; // To keep current image if no new image uploaded
  private String removeImage; // Flag to remove existing image
}