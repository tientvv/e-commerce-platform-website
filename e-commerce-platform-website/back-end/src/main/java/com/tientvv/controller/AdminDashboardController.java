package com.tientvv.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tientvv.model.Account;
import com.tientvv.repository.AccountRepository;
import com.tientvv.repository.CategoryRepository;
import com.tientvv.repository.ShopRepository;
import com.tientvv.repository.ProductRepository;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private AccountRepository accountRepository;

  @Autowired
  private ShopRepository shopRepository;

  @Autowired
  private ProductRepository productRepository;

  // Check admin role helper method
  private ResponseEntity<Map<String, Object>> checkAdminRole(HttpSession session) {
    Account account = (Account) session.getAttribute("account");
    Map<String, Object> response = new HashMap<>();

    if (account == null) {
      response.put("message", "Bạn chưa đăng nhập!");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    if (!"ADMIN".equals(account.getRole())) {
      response.put("message", "Bạn không có quyền truy cập!");
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    return null; // No error
  }

  @GetMapping("/stats")
  public ResponseEntity<Map<String, Object>> getDashboardStats(HttpSession session) {
    ResponseEntity<Map<String, Object>> authCheck = checkAdminRole(session);
    if (authCheck != null)
      return authCheck;

    Map<String, Object> response = new HashMap<>();
    try {
      // Đếm tổng số danh mục
      long totalCategories = categoryRepository.count();
      
      // Đếm tổng số người dùng (không bao gồm ADMIN)
      long totalUsers = accountRepository.countByRoleNot();
      
      // Đếm tổng số cửa hàng
      long totalShops = shopRepository.count();
      
      // Đếm tổng số sản phẩm
      long totalProducts = productRepository.count();
      
      // Tạo response data
      Map<String, Object> stats = new HashMap<>();
      stats.put("totalCategories", totalCategories);
      stats.put("totalUsers", totalUsers);
      stats.put("totalShops", totalShops);
      stats.put("totalProducts", totalProducts);
      
      response.put("stats", stats);
      response.put("message", "Lấy thống kê dashboard thành công!");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      response.put("message", "Lỗi khi lấy thống kê dashboard: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }
}
