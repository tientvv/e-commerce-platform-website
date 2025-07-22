package com.tientvv.dto.shop;

import lombok.Data;

@Data
public class ShopDto {
  private String shopName;
  private String description;
  private String email;
  private String phone;
  private String address;
  private String shopImage;
  private Boolean isActive;
}
