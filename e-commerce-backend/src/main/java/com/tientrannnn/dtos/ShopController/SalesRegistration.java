package com.tientrannnn.dtos.ShopController;

import java.util.UUID;
import lombok.Data;

@Data
public class SalesRegistration {
  private UUID userId;
  private String reason;
  private Integer status;
}
