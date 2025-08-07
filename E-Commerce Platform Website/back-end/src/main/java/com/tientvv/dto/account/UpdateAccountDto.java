package com.tientvv.dto.account;

import lombok.Data;
import java.util.UUID;

@Data
public class UpdateAccountDto {
  private UUID id;
  private String username;
  private String name;
  private String email;
  private String phone;
  private String address;
}
