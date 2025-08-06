package com.tientvv.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.tientvv.dto.CrudProduct.CreateProductDto;
import com.tientvv.dto.CrudProduct.ProductDto;
import com.tientvv.dto.CrudProduct.ProductDetailDto;
import com.tientvv.dto.CrudProduct.ProductDisplayDto;
import com.tientvv.dto.CrudProduct.UpdateProductDto;
import com.tientvv.model.Account;
import com.tientvv.model.Product;
import com.tientvv.service.ProductService;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.tientvv.service.ImageUploadService;

@RestController
@RequestMapping("/api/products")
public class ProductController {

  @Autowired
  private ProductService productService;

  @Autowired
  private ImageUploadService imageUploadService;

  @Autowired
  private com.tientvv.service.ShopService shopService;

  @GetMapping("/user")
  public Map<String, Object> getUserProducts(HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");

    if (account == null) {
      response.put("message", "Bạn chưa đăng nhập!");
      return response;
    }

    try {
      // Lấy thông tin shop của user
      com.tientvv.dto.shop.ShopDto shopDto = shopService.getShopByUserId(account.getId());
      if (shopDto == null) {
        response.put("message", "Bạn chưa đăng ký cửa hàng!");
        response.put("products", List.of()); // Trả về danh sách rỗng
        return response;
      }

      // Lấy tất cả sản phẩm của shop (bao gồm cả active và inactive)
      List<Product> products = productService.findAllByShopId(shopDto.getId());

      // Convert to DTOs
      List<ProductDto> productDtos = products.stream()
          .map(ProductDto::fromEntity)
          .collect(Collectors.toList());

      response.put("products", productDtos);
    } catch (Exception e) {
      response.put("message", "Lỗi khi tải danh sách sản phẩm: " + e.getMessage());
      response.put("products", List.of());
    }

    return response;
  }

  @GetMapping
  public Map<String, Object> getProducts() {
    Map<String, Object> response = new HashMap<>();
    try {
      List<ProductDisplayDto> products = productService.findActiveProductsWithPricing();

      response.put("products", products);
    } catch (Exception e) {
      response.put("message", e.getMessage());
    }
    return response;
  }

  @GetMapping("/all")
  public Map<String, Object> getAllProducts(
      @RequestParam(required = false) String shopId,
      @RequestParam(required = false) String categoryId) {
    Map<String, Object> response = new HashMap<>();
    try {
      if (categoryId != null && !categoryId.isEmpty()) {
        UUID categoryUuid = UUID.fromString(categoryId);
        List<ProductDisplayDto> products = productService.findActiveProductsWithPricingByCategoryId(categoryUuid);
        response.put("products", products);
      } else if (shopId != null && !"your-shop-id".equals(shopId)) {
        UUID uuid = UUID.fromString(shopId);
        List<Product> products = productService.findAllByShopIdAndIsActiveTrue(uuid);
        List<ProductDto> productDtos = products.stream()
            .map(ProductDto::fromEntity)
            .collect(Collectors.toList());
        response.put("products", productDtos);
      } else {
        List<ProductDisplayDto> products = productService.findActiveProductsWithPricing();
        response.put("products", products);
      }
    } catch (Exception e) {
      response.put("message", e.getMessage());
    }
    return response;
  }

  @GetMapping("/index")
  public Map<String, Object> getAllProductsByShop(
      @RequestParam UUID shopId,
      @RequestParam Boolean isActive,
      HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");
    if (account == null) {
      response.put("message", "Bạn chưa đăng nhập!");
      return response;
    }

    List<ProductDto> products = productService.getProductByShopIdAndIsActive(shopId, isActive);
    response.put("products", products);
    return response;
  }

  @PostMapping("/add")
  public Map<String, Object> createProduct(@ModelAttribute CreateProductDto dto, HttpSession session) throws Exception {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");
    if (account == null) {
      response.put("message", "Bạn chưa đăng nhập!");
      return response;
    }

    // Lấy shopId từ user session
    try {
      com.tientvv.dto.shop.ShopDto shopDto = shopService.getShopByUserId(account.getId());
      if (shopDto == null) {
        response.put("message", "Bạn chưa đăng ký cửa hàng!");
        return response;
      }
      dto.setShopId(shopDto.getId());
    } catch (Exception e) {
      response.put("message", "Lỗi khi lấy thông tin cửa hàng: " + e.getMessage());
      return response;
    }

    if (dto.getName().isEmpty() || dto.getBrand().isEmpty() || dto.getDescription().isEmpty()) {
      response.put("message", "Vui lòng điền đầy đủ thông tin sản phẩm!");
      return response;
    }
    if (dto.getCategoryId() == null) {
      response.put("errorMessage", "Vui lòng chọn danh mục cho sản phẩm!");
      return response;
    }
    if (dto.getProductImage() == null || dto.getProductImage().isEmpty()) {
      response.put("errorMessage", "Vui lòng chọn hình ảnh cho sản phẩm!");
      return response;
    }

    productService.createProduct(dto);
    response.put("message", "Sản phẩm đã được tạo thành công!");
    return response;
  }

  @PostMapping("/upload-image")
  public Map<String, Object> uploadImage(@RequestParam("file") MultipartFile file, HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");
    if (account == null) {
      response.put("message", "Bạn chưa đăng nhập!");
      return response;
    }

    try {
      if (file == null || file.isEmpty()) {
        response.put("message", "Vui lòng chọn file ảnh!");
        return response;
      }

      String imageUrl = imageUploadService.uploadImage(file);
      response.put("message", "Upload ảnh thành công!");
      response.put("url", imageUrl);
    } catch (Exception e) {
      response.put("message", "Lỗi khi upload ảnh: " + e.getMessage());
    }
    return response;
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getProductById(@PathVariable UUID id) {
    ProductDetailDto productDetail = productService.getProductDetailById(id);
    if (productDetail == null) {
      Map<String, String> response = new HashMap<>();
      response.put("message", "Không tìm thấy sản phẩm!");
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    return ResponseEntity.ok(productDetail);
  }

  @PutMapping("/update/{id}")
  public Map<String, Object> updateProduct(@ModelAttribute UpdateProductDto dto, @PathVariable UUID id,
      HttpSession session)
      throws Exception {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");
    if (account == null) {
      response.put("message", "Bạn chưa đăng nhập!");
      return response;
    }
    if (dto.getName() == null || dto.getBrand() == null || dto.getDescription() == null) {
      response.put("message", "Vui lòng điền đầy đủ thông tin sản phẩm!");
      return response;
    }
    productService.updateProduct(id, dto);
    response.put("message", "Sản phẩm đã được cập nhật thành công!");
    return response;
  }

  @PutMapping("/delete/{id}")
  public Map<String, Object> deleteProduct(@PathVariable UUID id, HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");
    if (account == null) {
      response.put("message", "Bạn chưa đăng nhập!");
      return response;
    }
    if (id == null) {
      response.put("message", "ID sản phẩm không hợp lệ!");
      return response;
    }
    productService.deleteProduct(id);
    response.put("message", "Đã ẩn sản phẩm thành công!");
    return response;
  }

  @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
  public Map<String, Object> deleteProductREST(@PathVariable UUID id, HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");
    if (account == null) {
      response.put("message", "Bạn chưa đăng nhập!");
      return response;
    }
    if (id == null) {
      response.put("message", "ID sản phẩm không hợp lệ!");
      return response;
    }
    productService.deleteProduct(id);
    response.put("message", "Đã ẩn sản phẩm thành công!");
    return response;
  }

  @PutMapping("/restore/{id}")
  public Map<String, Object> restoreProduct(@PathVariable UUID id, HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");
    if (account == null) {
      response.put("message", "Bạn chưa đăng nhập!");
      return response;
    }
    if (id == null) {
      response.put("message", "ID sản phẩm không hợp lệ!");
      return response;
    }
    productService.restoreProduct(id);
    response.put("message", "Đã khôi phục sản phẩm thành công!");
    return response;
  }
}