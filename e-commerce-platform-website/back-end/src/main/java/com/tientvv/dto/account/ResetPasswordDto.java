package com.tientvv.dto.account;

import lombok.Data;

@Data
public class ResetPasswordDto {
    private String token;
    private String newPassword;
    private String confirmPassword;
}
