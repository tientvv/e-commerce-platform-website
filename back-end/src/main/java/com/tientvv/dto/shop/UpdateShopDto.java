package com.tientvv.dto.shop;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateShopDto {
  private String shopName;
  private String description;
  private String phone;
  private String email;
  private String address;
  private String shopImage;
}