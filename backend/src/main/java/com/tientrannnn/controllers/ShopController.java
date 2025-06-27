package com.tientrannnn.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tientrannnn.dtos.ShopRegisterRequest;
import com.tientrannnn.models.Shop;
import com.tientrannnn.models.User;
import com.tientrannnn.repositories.ShopRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api")
public class ShopController {

  @Autowired
  private ShopRepository shopRepository;

  @PostMapping("register-seller")
  public ResponseEntity<?> registerSeller(@RequestBody ShopRegisterRequest request, HttpSession session) {
    User user = (User) session.getAttribute("user");

    if (user == null) {
      return ResponseEntity.status(401).body("User not authenticated");
    }
    if (shopRepository.existsByUserId(user.getId())) {
      return ResponseEntity.badRequest().body("Shop already exists for this user");
    }
    Shop shop = new Shop();
    shop.setNameShop(request.getNameShop());
    shop.setAvatarUrl(request.getAvatarUrl());
    shop.setCccd(request.getCccd());
    shop.setCccdFrontUrl(request.getCccdFrontUrl());
    shop.setCccdBackUrl(request.getCccdBackUrl());
    shop.setReason("Đang chờ duyệt");
    shop.setUser(user);

    shopRepository.save(shop);
    return ResponseEntity.ok("Shop created successfully");
  }

}
