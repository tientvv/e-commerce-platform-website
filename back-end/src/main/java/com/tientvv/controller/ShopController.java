package com.tientvv.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.tientvv.dto.shop.RegisterShopDto;
import com.tientvv.dto.shop.ShopDto;
import com.tientvv.model.Account;
import com.tientvv.model.Shop;
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
    ShopDto shop = shopService.getShopByUserId(account.getId());
    response.put("shop", shop);
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
}
