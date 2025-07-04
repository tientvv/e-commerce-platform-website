package com.tientrannnn.dtos.ShopController;

import lombok.Data;

@Data
public class RegisterRequest {
  private String nameShop;
  private String avatarShop;
  private String cccd;
  private String cccdFrontUrl;
  private String cccdBackUrl;
}
