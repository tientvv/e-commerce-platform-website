package com.tientvv.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tientvv.dto.shop.RegisterShopDto;
import com.tientvv.dto.shop.ShopDto;
import com.tientvv.dto.shop.UpdateShopDto;
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
    dto.setId(shop.getId());
    dto.setShopName(shop.getShopName());
    dto.setDescription(shop.getDescription());
    dto.setEmail(shop.getEmail());
    dto.setPhone(shop.getPhone());
    dto.setAddress(shop.getAddress());
    dto.setShopImage(shop.getShopImage());
    dto.setIsActive(shop.getIsActive());
    dto.setCreatedAt(shop.getCreatedAt());
    dto.setLastUpdated(shop.getLastUpdated());
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

  public Shop updateShopInfo(UUID userId, UpdateShopDto dto) throws Exception {
    Shop shop = shopRepository.findByUserId(userId);
    if (shop == null) {
      throw new RuntimeException("Không tìm thấy cửa hàng");
    }

    // Kiểm tra thời gian cập nhật cuối (24h = 1440 phút)
    if (shop.getLastUpdated() != null) {
      OffsetDateTime now = OffsetDateTime.now();
      Duration duration = Duration.between(shop.getLastUpdated(), now);
      long hoursSinceLastUpdate = duration.toHours();

      if (hoursSinceLastUpdate < 24) {
        double hoursRemaining = 24 - (duration.toMinutes() / 60.0);
        long wholeHours = (long) Math.floor(hoursRemaining);
        long minutes = Math.round((hoursRemaining - wholeHours) * 60);

        String timeMessage = wholeHours > 0
            ? (minutes > 0 ? wholeHours + " giờ " + minutes + " phút" : wholeHours + " giờ")
            : minutes + " phút";

        throw new RuntimeException("Bạn chỉ có thể cập nhật thông tin cửa hàng 1 lần trong 24 giờ. " +
            "Vui lòng đợi thêm " + timeMessage + " nữa.");
      }
    }

    // Cập nhật thông tin
    if (dto.getShopName() != null && !dto.getShopName().trim().isEmpty()) {
      shop.setShopName(dto.getShopName().trim());
    }
    if (dto.getDescription() != null) {
      shop.setDescription(dto.getDescription().trim());
    }
    if (dto.getPhone() != null && !dto.getPhone().trim().isEmpty()) {
      shop.setPhone(dto.getPhone().trim());
    }
    if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
      shop.setEmail(dto.getEmail().trim());
    }
    if (dto.getAddress() != null && !dto.getAddress().trim().isEmpty()) {
      shop.setAddress(dto.getAddress().trim());
    }
    if (dto.getShopImage() != null && !dto.getShopImage().trim().isEmpty()) {
      shop.setShopImage(dto.getShopImage().trim());
    }

    // Cập nhật thời gian
    shop.setLastUpdated(OffsetDateTime.now());

    return shopRepository.save(shop);
  }

  public boolean canUpdateShop(UUID userId) {
    Shop shop = shopRepository.findByUserId(userId);
    if (shop == null || shop.getLastUpdated() == null) {
      return true; // Chưa từng cập nhật hoặc không tìm thấy shop
    }

    OffsetDateTime now = OffsetDateTime.now();
    Duration duration = Duration.between(shop.getLastUpdated(), now);
    return duration.toHours() >= 24;
  }

  public long getHoursUntilNextUpdate(UUID userId) {
    Shop shop = shopRepository.findByUserId(userId);
    if (shop == null || shop.getLastUpdated() == null) {
      return 0; // Có thể cập nhật ngay
    }

    OffsetDateTime now = OffsetDateTime.now();
    Duration duration = Duration.between(shop.getLastUpdated(), now);
    long hoursSinceLastUpdate = duration.toHours();

    return Math.max(0, 24 - hoursSinceLastUpdate);
  }

  public double getHoursUntilNextUpdatePrecise(UUID userId) {
    Shop shop = shopRepository.findByUserId(userId);
    if (shop == null || shop.getLastUpdated() == null) {
      return 0; // Có thể cập nhật ngay
    }

    OffsetDateTime now = OffsetDateTime.now();
    Duration duration = Duration.between(shop.getLastUpdated(), now);
    double hoursSinceLastUpdate = duration.toMinutes() / 60.0;

    return Math.max(0, 24 - hoursSinceLastUpdate);
  }

  public List<Shop> getAllShops() {
    return shopRepository.findAll();
  }

  public ShopDto convertToDto(Shop shop) {
    ShopDto dto = new ShopDto();
    dto.setId(shop.getId());
    dto.setShopName(shop.getShopName());
    dto.setDescription(shop.getDescription());
    dto.setEmail(shop.getEmail());
    dto.setPhone(shop.getPhone());
    dto.setAddress(shop.getAddress());
    dto.setShopImage(shop.getShopImage());
    dto.setIsActive(shop.getIsActive());
    dto.setCreatedAt(shop.getCreatedAt());
    dto.setLastUpdated(shop.getLastUpdated());
    return dto;
  }

  public Shop createDemoShop() throws Exception {
    // Check if demo shop already exists
    List<Shop> existingShops = shopRepository.findAll();
    if (!existingShops.isEmpty()) {
      return existingShops.get(0); // Return first existing shop
    }

    // Get admin account for shop owner
    Account adminAccount = accountRepository.findByUsernameAndIsActive("admin", true);
    if (adminAccount == null) {
      throw new RuntimeException("Admin account not found, cannot create demo shop");
    }

    // Create demo shop
    Shop shop = new Shop();
    shop.setShopName("Demo Shop");
    shop.setDescription("Cửa hàng demo cho testing");
    shop.setAddress("123 Đường Demo, Quận 1, TP.HCM");
    shop.setPhone("0123456789");
    shop.setEmail("demo@shop.com");
    shop.setShopImage("https://example.com/shop-logo.png");
    shop.setRating(new BigDecimal("4.5"));
    shop.setIsActive(true);
    shop.setUser(adminAccount);
    shop.setCreatedAt(OffsetDateTime.now());
    shop.setLastUpdated(OffsetDateTime.now());

    return shopRepository.save(shop);
  }
}
