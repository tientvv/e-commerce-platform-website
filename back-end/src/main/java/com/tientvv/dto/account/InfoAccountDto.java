package com.tientvv.dto.account;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InfoAccountDto {
  private String username;
  private String name;
  private String email;
  private String phone;
  private String role;
}
