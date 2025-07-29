package com.tientvv.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePaymentDto {
  @NotBlank(message = "Mã thanh toán không được để trống")
  @Size(max = 50, message = "Mã thanh toán không được quá 50 ký tự")
  private String paymentCode;

  @NotBlank(message = "Loại thanh toán không được để trống")
  @Size(max = 20, message = "Loại thanh toán không được quá 20 ký tự")
  private String paymentType;

  @NotBlank(message = "Tên thanh toán không được để trống")
  @Size(max = 100, message = "Tên thanh toán không được quá 100 ký tự")
  private String paymentName;

  private String icon;

  private String description;
}