package com.tientvv.controller;

import com.tientvv.dto.discount.CreateDiscountDto;
import com.tientvv.dto.discount.DiscountDto;
import com.tientvv.dto.discount.UpdateDiscountDto;
import com.tientvv.dto.Categories.CategoryDto;
import com.tientvv.dto.CrudProduct.ProductDto;
import com.tientvv.dto.CrudProduct.ProductVariantDto;
import com.tientvv.service.DiscountService;
import com.tientvv.service.AccountService;
import com.tientvv.service.CategoryService;
import com.tientvv.service.ProductService;
import com.tientvv.service.ProductVariantService;
import com.tientvv.model.Account;
import com.tientvv.model.Product;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/discounts")
@RequiredArgsConstructor
public class AdminDiscountController {

  private final DiscountService discountService;
  @SuppressWarnings("unused")
  private final AccountService accountService;
  private final CategoryService categoryService;
  private final ProductService productService;
  private final ProductVariantService productVariantService;

  // Check admin role for all endpoints
  private void checkAdminRole(HttpSession session) {
    Account account = (Account) session.getAttribute("account");
    if (account == null) {
      throw new RuntimeException("Vui lòng đăng nhập!");
    }

    if (!"ADMIN".equals(account.getRole())) {
      throw new RuntimeException("Bạn không có quyền truy cập!");
    }
  }

  // Helper method to handle validation errors
  private Map<String, Object> handleValidationErrors(BindingResult bindingResult) {
    Map<String, Object> response = new HashMap<>();
    response.put("success", false);

    List<String> errors = bindingResult.getFieldErrors().stream()
        .map(FieldError::getDefaultMessage)
        .collect(Collectors.toList());

    response.put("message", "Dữ liệu không hợp lệ: " + String.join(", ", errors));
    response.put("errors", errors);

    return response;
  }

  @GetMapping
  public ResponseEntity<Map<String, Object>> getAllDiscounts(HttpSession session) {
    checkAdminRole(session);

    try {
      List<DiscountDto> discounts = discountService.getAllDiscounts();

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("discounts", discounts);
      response.put("message", "Lấy danh sách giảm giá thành công");

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> response = new HashMap<>();
      response.put("success", false);
      response.put("message", e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  @GetMapping("/all-including-inactive")
  public ResponseEntity<Map<String, Object>> getAllDiscountsIncludingInactive(HttpSession session) {
    checkAdminRole(session);

    try {
      List<DiscountDto> discounts = discountService.getAllDiscountsIncludingInactive();

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("discounts", discounts);
      response.put("message", "Lấy danh sách tất cả giảm giá thành công");

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> response = new HashMap<>();
      response.put("success", false);
      response.put("message", e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  @GetMapping("/active")
  public ResponseEntity<Map<String, Object>> getActiveDiscounts(HttpSession session) {
    checkAdminRole(session);

    try {
      List<DiscountDto> discounts = discountService.getActiveDiscounts();

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("discounts", discounts);
      response.put("message", "Lấy danh sách giảm giá đang hoạt động thành công");

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> response = new HashMap<>();
      response.put("success", false);
      response.put("message", e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<Map<String, Object>> getDiscountById(@PathVariable UUID id, HttpSession session) {
    checkAdminRole(session);

    try {
      DiscountDto discount = discountService.getDiscountById(id);

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("discount", discount);
      response.put("message", "Lấy thông tin giảm giá thành công");

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> response = new HashMap<>();
      response.put("success", false);
      response.put("message", e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  @PostMapping
  public ResponseEntity<Map<String, Object>> createDiscount(@Valid @RequestBody CreateDiscountDto dto,
      BindingResult bindingResult, HttpSession session) {
    checkAdminRole(session);

    // Check validation errors
    if (bindingResult.hasErrors()) {
      return ResponseEntity.badRequest().body(handleValidationErrors(bindingResult));
    }

    try {
      DiscountDto discount = discountService.createDiscount(dto);

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("discount", discount);
      response.put("message", "Tạo giảm giá thành công: " + discount.getName());

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> response = new HashMap<>();
      response.put("success", false);
      response.put("message", e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<Map<String, Object>> updateDiscount(@PathVariable UUID id,
      @Valid @RequestBody UpdateDiscountDto dto, BindingResult bindingResult, HttpSession session) {
    checkAdminRole(session);

    // Check validation errors
    if (bindingResult.hasErrors()) {
      return ResponseEntity.badRequest().body(handleValidationErrors(bindingResult));
    }

    try {
      // Ensure ID consistency
      dto.setId(id);

      // Log payload for debugging
      System.out.println("Update Discount Payload: " + dto.toString());

      DiscountDto discount = discountService.updateDiscount(dto);

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("discount", discount);
      response.put("message", "Cập nhật giảm giá thành công: " + discount.getName());

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> response = new HashMap<>();
      response.put("success", false);
      response.put("message", e.getMessage());

      // Log detailed error for debugging
      System.err.println("Update discount error: " + e.getMessage());
      e.printStackTrace();

      return ResponseEntity.badRequest().body(response);
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Map<String, Object>> deleteDiscount(@PathVariable UUID id, HttpSession session) {
    checkAdminRole(session);

    try {
      discountService.deleteDiscount(id);

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("message", "Xóa giảm giá thành công");

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> response = new HashMap<>();
      response.put("success", false);
      response.put("message", e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  @DeleteMapping("/{id}/hard")
  public ResponseEntity<Map<String, Object>> hardDeleteDiscount(@PathVariable UUID id, HttpSession session) {
    checkAdminRole(session);

    try {
      discountService.hardDeleteDiscount(id);

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("message", "Xóa vĩnh viễn giảm giá thành công");

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> response = new HashMap<>();
      response.put("success", false);
      response.put("message", e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  // Helper endpoints for form data
  @GetMapping("/reference-data")
  public ResponseEntity<Map<String, Object>> getReferenceData(HttpSession session) {
    checkAdminRole(session);

    try {
      // Get categories
      List<CategoryDto> categories = categoryService.getAllCategories();

      // Get products
      List<Product> allProducts = productService.findAllActiveProducts();
      List<ProductDto> products = allProducts.stream()
          .map(ProductDto::fromEntity)
          .collect(java.util.stream.Collectors.toList());

      // Get variants
      List<ProductVariantDto> variants = productVariantService.getAllProductVariants();

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("categories", categories);
      response.put("products", products);
      response.put("variants", variants);
      response.put("discountTypes", List.of(
          Map.of("value", "PERCENTAGE", "label", "Phần trăm (%)"),
          Map.of("value", "FIXED", "label", "Số tiền cố định (VNĐ)")));
      response.put("applicationTypes", List.of(
          Map.of("value", "ALL", "label", "Áp dụng toàn bộ"),
          Map.of("value", "CATEGORY", "label", "Áp dụng theo danh mục"),
          Map.of("value", "PRODUCT", "label", "Áp dụng theo sản phẩm"),
          Map.of("value", "VARIANT", "label", "Áp dụng theo variant")));
      response.put("message", "Lấy dữ liệu tham chiếu thành công");

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> response = new HashMap<>();
      response.put("success", false);
      response.put("message", e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }
}