package com.tientvv.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tientvv.dto.Categories.CategoryDto;
import com.tientvv.dto.Categories.CreateCategoryDto;
import com.tientvv.dto.Categories.UpdateCategoryDto;
import com.tientvv.model.Account;
import com.tientvv.model.Category;
import com.tientvv.service.CategoryService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

  @Autowired
  private CategoryService categoryService;

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

  @GetMapping
  public ResponseEntity<Map<String, Object>> getAllCategories(HttpSession session) {
    ResponseEntity<Map<String, Object>> authCheck = checkAdminRole(session);
    if (authCheck != null)
      return authCheck;

    Map<String, Object> response = new HashMap<>();
    try {
      List<CategoryDto> categories = categoryService.getAllCategories();
      response.put("categories", categories);
      response.put("message", "Lấy danh sách danh mục thành công!");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      response.put("message", "Lỗi khi lấy danh sách danh mục: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<Map<String, Object>> getCategoryById(@PathVariable UUID id, HttpSession session) {
    ResponseEntity<Map<String, Object>> authCheck = checkAdminRole(session);
    if (authCheck != null)
      return authCheck;

    Map<String, Object> response = new HashMap<>();
    try {
      CategoryDto category = categoryService.getCategoryById(id);
      response.put("category", category);
      response.put("message", "Lấy thông tin danh mục thành công!");
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      response.put("message", e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    } catch (Exception e) {
      response.put("message", "Lỗi khi lấy thông tin danh mục: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @PostMapping
  public ResponseEntity<Map<String, Object>> createCategory(@ModelAttribute CreateCategoryDto dto,
      HttpSession session) {
    ResponseEntity<Map<String, Object>> authCheck = checkAdminRole(session);
    if (authCheck != null)
      return authCheck;

    Map<String, Object> response = new HashMap<>();
    try {
      // Validate input
      if (dto.getName() == null || dto.getName().trim().isEmpty()) {
        response.put("message", "Tên danh mục không được để trống!");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
      }

      Category category = categoryService.createCategory(dto);
      CategoryDto categoryDto = new CategoryDto(category.getId(), category.getName(), category.getCategoryImage());

      response.put("category", categoryDto);
      response.put("message", "Tạo danh mục thành công!");
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (RuntimeException e) {
      response.put("message", e.getMessage());
      return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    } catch (Exception e) {
      response.put("message", "Lỗi khi tạo danh mục: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<Map<String, Object>> updateCategory(
      @PathVariable UUID id,
      @ModelAttribute UpdateCategoryDto dto,
      HttpSession session) {
    ResponseEntity<Map<String, Object>> authCheck = checkAdminRole(session);
    if (authCheck != null)
      return authCheck;

    Map<String, Object> response = new HashMap<>();
    try {
      // Set the ID from path parameter
      dto.setId(id);

      // Validate input
      if (dto.getName() == null || dto.getName().trim().isEmpty()) {
        response.put("message", "Tên danh mục không được để trống!");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
      }

      Category category = categoryService.updateCategory(dto);
      CategoryDto categoryDto = new CategoryDto(category.getId(), category.getName(), category.getCategoryImage());

      response.put("category", categoryDto);
      response.put("message", "Cập nhật danh mục thành công!");
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      if (e.getMessage().contains("not found")) {
        response.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
      } else {
        response.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
      }
    } catch (Exception e) {
      response.put("message", "Lỗi khi cập nhật danh mục: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable UUID id, HttpSession session) {
    ResponseEntity<Map<String, Object>> authCheck = checkAdminRole(session);
    if (authCheck != null)
      return authCheck;

    Map<String, Object> response = new HashMap<>();
    try {
      categoryService.deleteCategory(id);
      response.put("message", "Xóa danh mục thành công!");
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      response.put("message", e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    } catch (Exception e) {
      response.put("message", "Lỗi khi xóa danh mục: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @DeleteMapping("/{id}/hard")
  public ResponseEntity<Map<String, Object>> hardDeleteCategory(@PathVariable UUID id, HttpSession session) {
    ResponseEntity<Map<String, Object>> authCheck = checkAdminRole(session);
    if (authCheck != null)
      return authCheck;

    Map<String, Object> response = new HashMap<>();
    try {
      categoryService.hardDeleteCategory(id);
      response.put("message", "Xóa vĩnh viễn danh mục thành công!");
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      if (e.getMessage().contains("existing products")) {
        response.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
      } else {
        response.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
      }
    } catch (Exception e) {
      response.put("message", "Lỗi khi xóa vĩnh viễn danh mục: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }
}