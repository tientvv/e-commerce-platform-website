package com.tientvv.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tientvv.dto.shop.RegisterShopDto;
import com.tientvv.dto.shop.ShopDto;
import com.tientvv.model.Account;
import com.tientvv.model.Shop;
import com.tientvv.repository.AccountRepository;
import com.tientvv.repository.ShopRepository;

@Service
public class ShopService {

  @Autowired
  private ShopRepository shopRepository;

  @Autowired
  private AccountRepository accountRepository;

  @Autowired
  private ImageUploadService imageUploadService;

  public ShopDto getShopByUserId(UUID userId) {
    Shop shop = shopRepository.findByUserId(userId);
    if (shop == null) {
      return null;
    }
    ShopDto dto = new ShopDto();
    dto.setShopName(shop.getShopName());
    dto.setDescription(shop.getDescription());
    dto.setEmail(shop.getEmail());
    dto.setPhone(shop.getPhone());
    dto.setAddress(shop.getAddress());
    dto.setShopImage(shop.getShopImage());
    dto.setIsActive(shop.getIsActive());
    return dto;
  }

  public Shop registerShop(RegisterShopDto dto) throws Exception {
    Shop shop = new Shop();
    shop.setShopName(dto.getShopName());
    shop.setDescription(dto.getDescription() == null ? "" : dto.getDescription());
    shop.setEmail(dto.getEmail());
    shop.setPhone(dto.getPhone());
    shop.setAddress(dto.getAddress());
    shop.setCreatedAt(OffsetDateTime.now());
    shop.setIsActive(true);
    Account account = accountRepository.findById(dto.getUser().getId())
        .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));
    shop.setUser(account);
    if (dto.getImage() != null && !dto.getImage().isEmpty()) {
      String imageUrl = imageUploadService.uploadImage(dto.getImage());
      shop.setShopImage(imageUrl);
    }
    return shopRepository.save(shop);
  }
}
