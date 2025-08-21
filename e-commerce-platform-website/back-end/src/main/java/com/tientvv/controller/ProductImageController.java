package com.tientvv.controller;

import java.util.ArrayList;
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

import com.tientvv.dto.CrudProduct.CreateProductImageDto;
import com.tientvv.dto.CrudProduct.ProductImageDto;
import com.tientvv.dto.CrudProduct.UpdateProductImageDto;
import com.tientvv.model.Account;
import com.tientvv.service.ProductImageService;
import com.tientvv.service.ShopService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/product-images")
public class ProductImageController {

  @Autowired
  private ProductImageService productImageService;

  @Autowired
  private ShopService shopService;

  @PostMapping("/create")
  public Map<String, Object> createProductImage(@RequestBody CreateProductImageDto dto, HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");
    if (account == null) {
      response.put("message", "Bạn chưa đăng nhập!");
      return response;
    }

    System.out.println("=== CREATE PRODUCT IMAGE DEBUG ===");
    System.out.println("Account ID: " + account.getId());
    System.out.println("DTO: " + dto);
    System.out.println("ProductId: " + dto.getProductId());
    System.out.println("ImageUrl: " + dto.getImageUrl());
    System.out.println("ProductVariantId: " + dto.getProductVariantId());

    try {
      if (dto.getProductId() == null || dto.getImageUrl() == null || dto.getImageUrl().isEmpty()) {
        response.put("message", "Vui lòng điền đầy đủ thông tin ảnh!");
        return response;
      }

      ProductImageDto createdImage = productImageService.createProductImage(dto);
      System.out.println("Created image: " + createdImage);

      response.put("success", true);
      response.put("message", "Tạo ảnh sản phẩm thành công!");
      response.put("image", createdImage);
    } catch (Exception e) {
      System.out.println("Error creating product image: " + e.getMessage());
      e.printStackTrace();
      response.put("success", false);
      response.put("message", e.getMessage());
    }
    return response;
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getProductImageById(@PathVariable UUID id) {
    try {
      ProductImageDto image = productImageService.getProductImageById(id);
      return ResponseEntity.ok(image);
    } catch (Exception e) {
      Map<String, String> response = new HashMap<>();
      response.put("message", e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
  }

  @GetMapping("/product/{productId}")
  public Map<String, Object> getProductImagesByProductId(@PathVariable String productId) {
    Map<String, Object> response = new HashMap<>();
    try {
      List<ProductImageDto> images;
      if ("all".equals(productId)) {
        // Lấy tất cả images
        images = productImageService.getAllProductImages();
      } else {
        // Lấy images theo product ID
        UUID uuid = UUID.fromString(productId);
        images = productImageService.getProductImagesByProductId(uuid);
      }
      response.put("images", images);
    } catch (Exception e) {
      response.put("message", e.getMessage());
    }
    return response;
  }

  @GetMapping("/product/{productId}/main")
  public Map<String, Object> getProductMainImages(@PathVariable UUID productId) {
    Map<String, Object> response = new HashMap<>();
    try {
      List<ProductImageDto> images = productImageService.getProductMainImages(productId);
      response.put("images", images);
    } catch (Exception e) {
      response.put("message", e.getMessage());
    }
    return response;
  }

  @GetMapping("/product/{productId}/variants")
  public Map<String, Object> getProductVariantImages(@PathVariable UUID productId) {
    Map<String, Object> response = new HashMap<>();
    try {
      List<ProductImageDto> images = productImageService.getProductVariantImages(productId);
      response.put("images", images);
    } catch (Exception e) {
      response.put("message", e.getMessage());
    }
    return response;
  }

  @GetMapping("/variant/{productVariantId}")
  public Map<String, Object> getProductImagesByProductVariantId(@PathVariable UUID productVariantId) {
    Map<String, Object> response = new HashMap<>();
    try {
      List<ProductImageDto> images = productImageService.getProductImagesByProductVariantId(productVariantId);
      response.put("images", images);
    } catch (Exception e) {
      response.put("message", e.getMessage());
    }
    return response;
  }

  @GetMapping("/all")
  public Map<String, Object> getAllProductImages(HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");
    if (account == null) {
      response.put("message", "Bạn chưa đăng nhập!");
      return response;
    }

    try {
      // Lấy shop của user hiện tại
      com.tientvv.dto.shop.ShopDto shopDto = shopService.getShopByUserId(account.getId());
      if (shopDto == null) {
        response.put("message", "Bạn chưa đăng ký cửa hàng!");
        response.put("images", new ArrayList<>());
        return response;
      }

      // Lấy tất cả ảnh của các sản phẩm thuộc shop này
      List<ProductImageDto> images = productImageService.getProductImagesByShopId(shopDto.getId());
      response.put("images", images);
    } catch (Exception e) {
      response.put("message", e.getMessage());
      response.put("images", new ArrayList<>());
    }
    return response;
  }

  @PutMapping("/update/{id}")
  public Map<String, Object> updateProductImage(@PathVariable UUID id, @RequestBody UpdateProductImageDto dto,
      HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");
    if (account == null) {
      response.put("message", "Bạn chưa đăng nhập!");
      return response;
    }

    try {
      if (dto.getProductId() == null) {
        response.put("message", "Vui lòng chọn sản phẩm!");
        return response;
      }

      ProductImageDto updatedImage = productImageService.updateProductImage(id, dto);
      response.put("message", "Cập nhật ảnh sản phẩm thành công!");
      response.put("image", updatedImage);
    } catch (Exception e) {
      response.put("message", e.getMessage());
    }
    return response;
  }

  @DeleteMapping("/delete/{id}")
  public Map<String, Object> deleteProductImage(@PathVariable UUID id, HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");
    if (account == null) {
      response.put("message", "Bạn chưa đăng nhập!");
      return response;
    }

    try {
      productImageService.deleteProductImage(id);
      response.put("message", "Xóa ảnh sản phẩm thành công!");
    } catch (Exception e) {
      response.put("message", e.getMessage());
    }
    return response;
  }

  @DeleteMapping("/delete/product/{productId}")
  public Map<String, Object> deleteProductImagesByProductId(@PathVariable UUID productId, HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");
    if (account == null) {
      response.put("message", "Bạn chưa đăng nhập!");
      return response;
    }

    try {
      productImageService.deleteProductImagesByProductId(productId);
      response.put("message", "Xóa tất cả ảnh sản phẩm thành công!");
    } catch (Exception e) {
      response.put("message", e.getMessage());
    }
    return response;
  }

  @DeleteMapping("/delete/variant/{productVariantId}")
  public Map<String, Object> deleteProductImagesByProductVariantId(@PathVariable UUID productVariantId,
      HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");
    if (account == null) {
      response.put("message", "Bạn chưa đăng nhập!");
      return response;
    }

    try {
      productImageService.deleteProductImagesByProductVariantId(productVariantId);
      response.put("message", "Xóa tất cả ảnh biến thể sản phẩm thành công!");
    } catch (Exception e) {
      response.put("message", e.getMessage());
    }
    return response;
  }
}