package com.tientrannnn.dtos;

import lombok.Data;

@Data
public class ShopRegisterRequest {
  private String nameShop;
  private String avatarUrl;
  private String cccd;
  private String cccdFrontUrl;
  private String cccdBackUrl;
}
