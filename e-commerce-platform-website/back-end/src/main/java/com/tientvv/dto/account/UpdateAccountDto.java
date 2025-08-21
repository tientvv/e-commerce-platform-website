package com.tientvv.dto.account;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAccountDto {
    private String username;
    private String name;
    private String email;
    private String phone;
    private String address;
}
