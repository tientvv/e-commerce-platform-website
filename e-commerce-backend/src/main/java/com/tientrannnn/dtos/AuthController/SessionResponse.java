package com.tientrannnn.dtos.AuthController;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.tientrannnn.models.Shop;
import com.tientrannnn.models.User;
import lombok.Data;

@Data
public class SessionResponse {
  private UUID id;
  private String email;
  private String avatarUrl;
  private String role;
  private Boolean isSeller;
  private Boolean isDeleted;
  private String fullName;
  private LocalDate birthday;
  private Boolean gender;
  private String phone;
  private String address;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;

  private Integer status;
  private String reason;

  public SessionResponse(User user) {
    this.id = user.getId();
    this.email = user.getEmail();
    this.avatarUrl = user.getAvatarUrl();
    this.role = user.getRole();
    this.isSeller = user.getIsSeller();
    this.isDeleted = user.getIsDeleted();
    this.fullName = user.getFullName();
    this.birthday = user.getBirthday();
    this.gender = user.getGender();
    this.phone = user.getPhone();
    this.address = user.getAddress();
    this.createdAt = user.getCreatedAt();
    this.updatedAt = user.getUpdatedAt();

    Shop shop = user.getShop();

    if (shop != null) {
      this.status = shop.getStatus();
      this.reason = shop.getReason();
    } else {
      this.status = null;
      this.reason = null;
    }
  }
}
