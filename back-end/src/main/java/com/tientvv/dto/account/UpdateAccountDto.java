package com.tientvv.dto.account;

import lombok.Data;

@Data
public class UpdateAccountDto {
  private String username;
  private String name;
  private String email;
  private String phone;
}
