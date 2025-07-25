package com.tientvv.dto.CrudProduct;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;
import lombok.Data;

@Data
public class CreateProductDto {
  private UUID categoryId;
  private String brand;
  private String name;
  private MultipartFile productImage;
  private String description;
  private UUID shopId;
}
