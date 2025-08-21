package com.tientvv.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tientvv.dto.CrudProduct.CreateProductVariantDto;
import com.tientvv.dto.CrudProduct.ProductVariantDto;
import com.tientvv.dto.CrudProduct.UpdateProductVariantDto;
import com.tientvv.model.Account;
import com.tientvv.service.ProductVariantService;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/product-variants")
public class ProductVariants {

  @Autowired
  private ProductVariantService productVariantService;

  @GetMapping
  public Map<String, Object> getProductVariants() {
    Map<String, Object> response = new HashMap<>();
    try {
      List<ProductVariantDto> variants = productVariantService.getAllProductVariants();
      response.put("variants", variants);
    } catch (Exception e) {
      response.put("message", e.getMessage());
    }
    return response;
  }

  @PostMapping("/create")
  public Map<String, Object> createProductVariant(@RequestBody CreateProductVariantDto dto, HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");
    if (account == null) {
      response.put("message", "Bạn chưa đăng nhập!");
      return response;
    }

    try {
      if (dto.getProductId() == null || dto.getVariantName() == null || dto.getVariantValue() == null ||
          dto.getQuantity() == null || dto.getPrice() == null) {
        response.put("message", "Vui lòng điền đầy đủ thông tin biến thể!");
        return response;
      }

      ProductVariantDto createdVariant = productVariantService.createProductVariant(dto);
      response.put("message", "Tạo biến thể sản phẩm thành công!");
      response.put("variant", createdVariant);
    } catch (Exception e) {
      response.put("message", e.getMessage());
    }
    return response;
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getProductVariantById(@PathVariable UUID id) {
    try {
      ProductVariantDto variant = productVariantService.getProductVariantById(id);
      return ResponseEntity.ok(variant);
    } catch (Exception e) {
      Map<String, String> response = new HashMap<>();
      response.put("message", e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
  }

  @GetMapping("/product/{productId}")
  public Map<String, Object> getProductVariantsByProductId(@PathVariable String productId) {
    Map<String, Object> response = new HashMap<>();
    try {
      List<ProductVariantDto> variants;
      if ("all".equals(productId)) {
        // Lấy tất cả variants
        variants = productVariantService.getAllProductVariants();
      } else {
        // Lấy variants theo product ID
        UUID uuid = UUID.fromString(productId);
        variants = productVariantService.getProductVariantsByProductId(uuid);
      }
      
      // Log để debug
      System.out.println("Product variants for product ID: " + productId);
      for (ProductVariantDto variant : variants) {
        System.out.println("  - Variant ID: " + variant.getId());
        System.out.println("  - VariantName: " + variant.getVariantName());
        System.out.println("  - VariantValue: " + variant.getVariantValue());
      }
      
      response.put("variants", variants);
    } catch (Exception e) {
      response.put("message", e.getMessage());
    }
    return response;
  }

  @GetMapping("/all")
  public Map<String, Object> getAllProductVariants() {
    Map<String, Object> response = new HashMap<>();
    try {
      List<ProductVariantDto> variants = productVariantService.getAllProductVariants();
      
      // Debug: Log all variants to check data
      System.out.println("=== DEBUG ALL PRODUCT VARIANTS ===");
      System.out.println("Total variants: " + variants.size());
      for (ProductVariantDto variant : variants) {
        System.out.println("Variant ID: " + variant.getId());
        System.out.println("  - Product ID: " + variant.getProductId());
        System.out.println("  - VariantName: " + variant.getVariantName());
        System.out.println("  - VariantValue: " + variant.getVariantValue());
        System.out.println("  - Price: " + variant.getPrice());
        System.out.println("  - Quantity: " + variant.getQuantity());
        System.out.println("  - IsActive: " + variant.getIsActive());
        System.out.println("---");
      }
      System.out.println("=== END DEBUG ALL PRODUCT VARIANTS ===");
      
      response.put("variants", variants);
    } catch (Exception e) {
      response.put("message", e.getMessage());
    }
    return response;
  }

  @PutMapping("/update/{id}")
  public Map<String, Object> updateProductVariant(@PathVariable UUID id, @RequestBody UpdateProductVariantDto dto,
      HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");
    if (account == null) {
      response.put("message", "Bạn chưa đăng nhập!");
      return response;
    }

    try {
      ProductVariantDto updatedVariant = productVariantService.updateProductVariant(id, dto);
      response.put("message", "Cập nhật biến thể sản phẩm thành công!");
      response.put("variant", updatedVariant);
    } catch (Exception e) {
      response.put("message", e.getMessage());
    }
    return response;
  }

  @DeleteMapping("/delete/{id}")
  public Map<String, Object> deleteProductVariant(@PathVariable UUID id, HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");
    if (account == null) {
      response.put("message", "Bạn chưa đăng nhập!");
      return response;
    }

    try {
      productVariantService.deleteProductVariant(id);
      response.put("message", "Xóa biến thể sản phẩm thành công!");
    } catch (Exception e) {
      response.put("message", e.getMessage());
    }
    return response;
  }

  @PostMapping("/fix-variant-data")
  public Map<String, Object> fixVariantData(HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");
    if (account == null) {
      response.put("message", "Bạn chưa đăng nhập!");
      return response;
    }

    try {
      // Lấy tất cả variants
      List<ProductVariantDto> variants = productVariantService.getAllProductVariants();
      int fixedCount = 0;
      
      System.out.println("=== FIXING VARIANT DATA ===");
      for (ProductVariantDto variant : variants) {
        boolean needsUpdate = false;
        UpdateProductVariantDto updateDto = new UpdateProductVariantDto();
        
        // Kiểm tra và sửa variantName
        if (variant.getVariantName() == null || variant.getVariantName().trim().isEmpty()) {
          updateDto.setVariantName("Loại");
          needsUpdate = true;
          System.out.println("Fixing variantName for variant ID: " + variant.getId());
        }
        
        // Kiểm tra và sửa variantValue
        if (variant.getVariantValue() == null || variant.getVariantValue().trim().isEmpty()) {
          updateDto.setVariantValue("Mặc định");
          needsUpdate = true;
          System.out.println("Fixing variantValue for variant ID: " + variant.getId());
        }
        
        // Cập nhật nếu cần
        if (needsUpdate) {
          productVariantService.updateProductVariant(variant.getId(), updateDto);
          fixedCount++;
        }
      }
      
      System.out.println("Fixed " + fixedCount + " variants");
      System.out.println("=== END FIXING VARIANT DATA ===");
      
      response.put("success", true);
      response.put("message", "Đã sửa " + fixedCount + " biến thể sản phẩm");
      response.put("fixedCount", fixedCount);
    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "Lỗi sửa dữ liệu: " + e.getMessage());
    }
    return response;
  }
}
