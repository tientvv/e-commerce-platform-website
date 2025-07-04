package com.tientrannnn.dtos.ShopController;

import java.util.UUID;

import lombok.Data;

@Data
public class ShowSalesRegistration {
  private String reason;
  private Integer status;
  private String nameShop;
  private String avatarShop;
  private String cccd;
  private String cccdFrontUrl;
  private String cccdBackUrl;
  private boolean isSeller;
  private UUID userId;
}
