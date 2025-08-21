package com.tientvv.dto.shop;

import org.springframework.web.multipart.MultipartFile;

import com.tientvv.model.Account;

import lombok.Data;

@Data
public class RegisterShopDto {
  private String shopName;
  private String description;
  private String email;
  private String phone;
  private String address;
  private MultipartFile image;
  private Account user;
}
