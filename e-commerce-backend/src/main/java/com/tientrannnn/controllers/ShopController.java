package com.tientrannnn.controllers;

import com.tientrannnn.dtos.AuthController.SessionResponse;
import com.tientrannnn.dtos.ShopController.*;
import com.tientrannnn.models.*;
import com.tientrannnn.repositories.*;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shops")
public class ShopController {

  @Autowired
  private ShopRepository shopRepository;

  @Autowired
  private UserRepository userRepository;

  @PostMapping("register")
  @Transactional
  public ResponseEntity<?> registerShop(HttpSession session, @RequestBody RegisterRequest request) {
    User currentUserInSession = (User) session.getAttribute("user");

    if (currentUserInSession == null) {
      return ResponseEntity.status(401).body("User not authenticated");
    }

    if (shopRepository.existsByUserId(currentUserInSession.getId())) {
      return ResponseEntity.badRequest().body("Shop already exists for this user");
    }

    if (shopRepository.existsByCccd(request.getCccd())) {
      return ResponseEntity.badRequest().body("This CCCD number is already registered to another shop.");
    }

    User userToUpdate = userRepository.findById(currentUserInSession.getId()).orElse(null);
    if (userToUpdate == null) {
      return ResponseEntity.status(404).body("User not found in database.");
    }

    Shop shop = new Shop();
    shop.setNameShop(request.getNameShop());
    shop.setAvatarShop(request.getAvatarShop());
    shop.setCccd(request.getCccd());
    shop.setCccdFrontUrl(request.getCccdFrontUrl());
    shop.setCccdBackUrl(request.getCccdBackUrl());
    shop.setStatus(1);
    shop.setUser(userToUpdate);
    shopRepository.save(shop);

    session.setAttribute("user", userToUpdate);

    return ResponseEntity.ok(new SessionResponse(userToUpdate));
  }

  @PutMapping("sales-registration")
  public ResponseEntity<?> salesRegistration(HttpSession session, @RequestBody SalesRegistration request) {
    Shop shop = shopRepository.findByUserId(request.getUserId())
        .orElse(null);

    if (shop == null) {
      return ResponseEntity.status(404).body("Shop not found for this user.");
    }

    User user = shop.getUser();
    shop.setStatus(request.getStatus());
    shop.setReason(request.getReason());
    if (request.getStatus() == 2) {
      user.setIsSeller(true);
    }

    shop.setUpdatedAt(OffsetDateTime.now());
    user.setUpdatedAt(OffsetDateTime.now());

    shopRepository.save(shop);
    userRepository.save(user);

    return ResponseEntity.ok("Shop status updated successfully.");
  }

  @DeleteMapping("/registrations/{userId}")
  @Transactional
  public ResponseEntity<?> deleteRegistration(@PathVariable UUID userId) {
    Shop shop = shopRepository.findByUserId(userId)
        .orElse(null);

    if (shop == null) {
      return ResponseEntity.status(404).body("Shop not found for this user.");
    }

    shopRepository.delete(shop);

    return ResponseEntity.ok("Shop registration deleted successfully.");
  }

  @GetMapping("/registrations")
  public ResponseEntity<?> getAllSalesRegistrations(HttpSession session) {
    User admin = (User) session.getAttribute("user");
    if (admin == null || !"ADMIN".equals(admin.getRole())) {
      return ResponseEntity.status(403).body("Permission denied");
    }

    List<Shop> allShops = shopRepository.findByIsDeletedFalse();

    List<ShowSalesRegistration> responseList = allShops.stream().map(shop -> {
      ShowSalesRegistration dto = new ShowSalesRegistration();

      User shopOwner = shop.getUser();

      dto.setNameShop(shop.getNameShop());
      dto.setAvatarShop(shop.getAvatarShop());
      dto.setCccd(shop.getCccd());
      dto.setCccdFrontUrl(shop.getCccdFrontUrl());
      dto.setCccdBackUrl(shop.getCccdBackUrl());
      dto.setStatus(shop.getStatus());
      dto.setReason(shop.getReason());
      // Set other fields from the shop
      dto.setUserId(shopOwner.getId());
      dto.setSeller(shopOwner.getIsSeller());

      return dto;
    }).collect(Collectors.toList());

    return ResponseEntity.ok(responseList);
  }
}
