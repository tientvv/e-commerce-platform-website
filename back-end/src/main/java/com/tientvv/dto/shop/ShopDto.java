package com.tientvv.dto.shop;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class ShopDto {
  private UUID id;
  private String shopName;
  private String description;
  private String email;
  private String phone;
  private String address;
  private String shopImage;
  private Boolean isActive;
  private OffsetDateTime createdAt;
  private OffsetDateTime lastUpdated;
}
