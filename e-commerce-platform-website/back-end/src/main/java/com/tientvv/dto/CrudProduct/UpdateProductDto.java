package com.tientvv.dto.CrudProduct;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class UpdateProductDto {
  private UUID id;
  private String name;
  private String brand;
  private String description;
  private UUID categoryId;
  private UUID shopId;
  private MultipartFile productImage;
}
