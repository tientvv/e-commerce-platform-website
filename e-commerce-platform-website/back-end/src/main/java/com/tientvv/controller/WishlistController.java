package com.tientvv.controller;

import com.tientvv.model.Account;
import com.tientvv.service.WishlistService;
import com.tientvv.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

  private final WishlistService wishlistService;
  private final AccountService accountService;

  // Lấy danh sách wishlist
  @GetMapping
  public ResponseEntity<Map<String, Object>> getWishlist() {
    try {
      Account currentUser = accountService.getCurrentUserFromSession();
      if (currentUser == null) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", "Bạn cần phải đăng nhập để sử dụng tính năng này");
        return ResponseEntity.badRequest().body(errorResponse);
      }

      List<Map<String, Object>> items = wishlistService.getWishlistItems(currentUser.getId());

      Map<String, Object> response = new HashMap<>();
      response.put("items", items);
      response.put("message", "Lấy danh sách yêu thích thành công");

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("message", "Không thể lấy danh sách yêu thích: " + e.getMessage());
      return ResponseEntity.badRequest().body(errorResponse);
    }
  }

    // Thêm sản phẩm vào wishlist
  @PostMapping
  public ResponseEntity<Map<String, Object>> addToWishlist(@RequestBody Map<String, String> request) {
    try {
      Account currentUser = accountService.getCurrentUserFromSession();
      if (currentUser == null) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", "Bạn cần phải đăng nhập để sử dụng tính năng này");
        return ResponseEntity.badRequest().body(errorResponse);
      }
      UUID accountId = currentUser.getId();
      UUID productId = UUID.fromString(request.get("productId"));
      UUID variantId = request.get("variantId") != null ? UUID.fromString(request.get("variantId")) : null;
      
      wishlistService.addToWishlist(accountId, productId, variantId);
      
      Map<String, Object> response = new HashMap<>();
      response.put("message", "Đã thêm sản phẩm vào yêu thích");
      
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("message", "Không thể thêm vào yêu thích: " + e.getMessage());
      return ResponseEntity.badRequest().body(errorResponse);
    }
  }

    // Xóa sản phẩm khỏi wishlist
  @DeleteMapping("/{productId}")
  public ResponseEntity<Map<String, Object>> removeFromWishlist(@PathVariable String productId, @RequestParam(required = false) String variantId) {
    try {
      Account currentUser = accountService.getCurrentUserFromSession();
      if (currentUser == null) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", "Bạn cần phải đăng nhập để sử dụng tính năng này");
        return ResponseEntity.badRequest().body(errorResponse);
      }
      UUID accountId = currentUser.getId();
      UUID productUUID = UUID.fromString(productId);
      UUID variantUUID = variantId != null ? UUID.fromString(variantId) : null;
      
      wishlistService.removeFromWishlist(accountId, productUUID, variantUUID);
      
      Map<String, Object> response = new HashMap<>();
      response.put("message", "Đã bỏ yêu thích sản phẩm");
      
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("message", "Không thể bỏ yêu thích: " + e.getMessage());
      return ResponseEntity.badRequest().body(errorResponse);
    }
  }

  // Kiểm tra sản phẩm có trong wishlist không
  @GetMapping("/check/{productId}")
  public ResponseEntity<Map<String, Object>> checkWishlistStatus(@PathVariable String productId, @RequestParam(required = false) String variantId) {
    try {
      Account currentUser = accountService.getCurrentUserFromSession();
      if (currentUser == null) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", "Bạn cần phải đăng nhập để sử dụng tính năng này");
        return ResponseEntity.badRequest().body(errorResponse);
      }
      UUID accountId = currentUser.getId();
      UUID productUUID = UUID.fromString(productId);
      UUID variantUUID = variantId != null ? UUID.fromString(variantId) : null;

      boolean isInWishlist = wishlistService.isInWishlist(accountId, productUUID, variantUUID);

      Map<String, Object> response = new HashMap<>();
      response.put("isInWishlist", isInWishlist);
      response.put("message", "Kiểm tra trạng thái yêu thích thành công");

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("message", "Không thể kiểm tra trạng thái yêu thích: " + e.getMessage());
      return ResponseEntity.badRequest().body(errorResponse);
    }
  }

  // Toggle wishlist (thêm/bỏ yêu thích)
  @PostMapping("/toggle/{productId}")
  public ResponseEntity<Map<String, Object>> toggleWishlist(@PathVariable String productId, @RequestParam(required = false) String variantId) {
    try {
      Account currentUser = accountService.getCurrentUserFromSession();
      if (currentUser == null) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", "Bạn cần phải đăng nhập để sử dụng tính năng này");
        return ResponseEntity.badRequest().body(errorResponse);
      }
      UUID productUUID = UUID.fromString(productId);
      UUID variantUUID = variantId != null ? UUID.fromString(variantId) : null;
      boolean isAdded = wishlistService.toggleWishlist(currentUser.getId(), productUUID, variantUUID);

      Map<String, Object> response = new HashMap<>();
      response.put("isInWishlist", isAdded);
      response.put("message", isAdded ? "Đã thêm vào yêu thích" : "Đã bỏ yêu thích");

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("message", "Không thể thay đổi trạng thái yêu thích: " + e.getMessage());
      return ResponseEntity.badRequest().body(errorResponse);
    }
  }

  // Lấy số lượng wishlist cho một sản phẩm
  @GetMapping("/count/{productId}")
  public ResponseEntity<Map<String, Object>> getWishlistCount(@PathVariable String productId) {
    try {
      UUID productUUID = UUID.fromString(productId);
      long count = wishlistService.getWishlistCount(productUUID);
      
      Map<String, Object> response = new HashMap<>();
      response.put("count", count);
      response.put("message", "Lấy số lượng yêu thích thành công");
      
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("message", "Không thể lấy số lượng yêu thích: " + e.getMessage());
      return ResponseEntity.badRequest().body(errorResponse);
    }
  }

  // Lấy tổng số lượng wishlist của user hiện tại
  @GetMapping("/count")
  public ResponseEntity<Map<String, Object>> getUserWishlistCount() {
    try {
      Account currentUser = accountService.getCurrentUserFromSession();
      if (currentUser == null) {
        Map<String, Object> response = new HashMap<>();
        response.put("count", 0);
        response.put("message", "Chưa đăng nhập");
        return ResponseEntity.ok(response);
      }
      
      long count = wishlistService.getUserWishlistCount(currentUser.getId());
      
      Map<String, Object> response = new HashMap<>();
      response.put("count", count);
      response.put("message", "Lấy số lượng yêu thích thành công");
      
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("message", "Không thể lấy số lượng yêu thích: " + e.getMessage());
      return ResponseEntity.badRequest().body(errorResponse);
    }
  }
}
