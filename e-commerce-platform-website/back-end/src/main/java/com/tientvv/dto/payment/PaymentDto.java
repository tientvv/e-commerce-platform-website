package com.tientvv.dto.payment;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PaymentDto {
  private UUID id;
  private String paymentCode;
  private String paymentType;
  private String paymentName;
  private String icon;
  private String description;
  private Boolean isActive;
}