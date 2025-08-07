package com.tientvv.dto.account;

import lombok.Data;

@Data
public class RegisterAccountDto {
  private String name;
  private String username;
  private String email;
  private String password;
  private String phone;
}
