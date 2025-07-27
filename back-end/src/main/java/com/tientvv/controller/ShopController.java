package com.tientvv.controller;

import java.util.*;
import com.tientvv.model.*;
import com.tientvv.dto.shop.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.tientvv.repository.ShopRepository;
import com.tientvv.service.ShopService;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
public class ShopController {

  @Autowired
  private ShopService shopService;

  @Autowired
  private ShopRepository shopRepository;

  @GetMapping("/user/shop")
  public Map<String, Object> getShopByUserId(HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");
    if (account == null) {
      response.put("message", "Bạn chưa đăng nhập!");
      return response;
    }
    Shop existingShop = shopRepository.findByUserId(account.getId());
    if (existingShop == null) {
      response.put("message", "Bạn chưa đăng ký cửa hàng!");
      return response;
    } else if (!existingShop.getIsActive()) {
      response.put("message", "Cửa hàng của bạn đã bị khóa!");
      return response;
    }

    ShopDto shopDto = shopService.getShopByUserId(account.getId());
    response.put("shop", shopDto);

    // Thêm thông tin về khả năng cập nhật
    response.put("canUpdate", shopService.canUpdateShop(account.getId()));
    response.put("hoursUntilNextUpdate", shopService.getHoursUntilNextUpdatePrecise(account.getId()));

    return response;
  }

  @PostMapping("/register-shop")
  public Map<String, Object> register(@ModelAttribute RegisterShopDto dto, HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");
    if (account == null) {
      response.put("message", "Bạn chưa đăng nhập!");
      return response;
    }
    if (dto.getShopName().isEmpty() || dto.getEmail().isEmpty() ||
        dto.getPhone().isEmpty() || dto.getAddress().isEmpty()) {
      response.put("message", "Vui lòng điền đầy đủ thông tin cửa hàng!");
      return response;
    }
    try {
      if (dto.getImage() == null || dto.getImage().isEmpty()) {
        response.put("message", "Vui lòng chọn hình ảnh cho cửa hàng!");
        return response;
      }
      Shop existingShop = shopRepository.findByUserId(account.getId());
      if (existingShop == null) {
        dto.setUser(account);
        Shop shop = shopService.registerShop(dto);
        response.put("message", "Đăng ký cửa hàng thành công!");
        response.put("shop", shop);
        return response;
      } else if (existingShop.getIsActive() == false) {
        response.put("message", "Cửa hàng của bạn đã bị khóa!");
        return response;
      } else if (existingShop.getIsActive() == true) {
        response.put("message", "Bạn đã đăng ký cửa hàng rồi!");
        return response;
      }
    } catch (Exception e) {
      response.put("message", "Lỗi khi tải lên hình ảnh: " + e.getMessage());
      return response;
    }
    return response;
  }

  @PutMapping("/user/shop/update")
  public Map<String, Object> updateShopInfo(@RequestBody UpdateShopDto dto, HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");

    if (account == null) {
      response.put("message", "Bạn chưa đăng nhập!");
      return response;
    }

    try {
      // Thực hiện cập nhật thông tin
      shopService.updateShopInfo(account.getId(), dto);

      // Lấy thông tin cửa hàng mới sau khi cập nhật
      ShopDto shopDto = shopService.getShopByUserId(account.getId());

      response.put("message", "Cập nhật thông tin cửa hàng thành công!");
      response.put("shop", shopDto);
      response.put("canUpdate", false); // Sau khi cập nhật thì không thể cập nhật trong 24h
      response.put("hoursUntilNextUpdate", 24.0);

    } catch (Exception e) {
      response.put("message", e.getMessage());
    }

    return response;
  }

  @GetMapping("/user/shop/can-update")
  public Map<String, Object> checkCanUpdate(HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");

    if (account == null) {
      response.put("message", "Bạn chưa đăng nhập!");
      return response;
    }

    boolean canUpdate = shopService.canUpdateShop(account.getId());
    double hoursUntilNextUpdate = shopService.getHoursUntilNextUpdatePrecise(account.getId());

    response.put("canUpdate", canUpdate);
    response.put("hoursUntilNextUpdate", hoursUntilNextUpdate);

    if (!canUpdate) {
      response.put("message", "Bạn chỉ có thể cập nhật thông tin cửa hàng 1 lần trong 24 giờ. " +
          "Vui lòng đợi thêm " + hoursUntilNextUpdate + " giờ nữa.");
    }

    return response;
  }
}
