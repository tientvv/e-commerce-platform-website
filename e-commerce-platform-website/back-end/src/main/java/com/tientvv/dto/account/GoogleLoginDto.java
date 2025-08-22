package com.tientvv.dto.account;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleLoginDto {
    private String googleId;
    private String email;
    private String name;
    private String picture;
    
    @Override
    public String toString() {
        return "GoogleLoginDto{" +
                "googleId='" + googleId + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", picture='" + picture + '\'' +
                '}';
    }
}
