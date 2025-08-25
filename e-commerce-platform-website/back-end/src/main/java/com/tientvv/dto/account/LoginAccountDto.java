package com.tientvv.dto.account;

import lombok.Data;

@Data
public class LoginAccountDto {
  private String loginIdentifier; // Có thể là username hoặc email
  private String password;
}
