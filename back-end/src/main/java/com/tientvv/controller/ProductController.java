package com.tientvv.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.tientvv.dto.CrudProduct.CreateProductDto;
import com.tientvv.dto.CrudProduct.ProductDto;
import com.tientvv.dto.CrudProduct.UpdateProductDto;
import com.tientvv.model.Account;
import com.tientvv.model.Product;
import com.tientvv.service.ProductService;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/products")
public class ProductController {

  @Autowired
  private ProductService productService;

  @GetMapping("/all")
  public List<Product> getAllProducts(@RequestParam UUID shopId) {
    return productService.findAllByShopIdAndIsActiveTrue(shopId);
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

  @PostMapping("add")
  public Map<String, Object> createProduct(@ModelAttribute CreateProductDto dto, HttpSession session) throws Exception {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");
    if (account == null) {
      response.put("message", "Bạn chưa đăng nhập!");
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
    if (dto.getProductImage().isEmpty()) {
      response.put("errorMessage", "Vui lòng chọn hình ảnh cho sản phẩm!");
      return response;
    }
    productService.createProduct(dto);
    response.put("message", "Sản phẩm đã được tạo thành công!");
    return response;
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getProductById(@PathVariable UUID id) {
    ProductDto productDto = productService.getProductDtoById(id);
    if (productDto == null) {
      Map<String, String> response = new HashMap<>();
      response.put("message", "Không tìm thấy sản phẩm!");
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    return ResponseEntity.ok(productDto);
  }

  @PutMapping("update/{id}")
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

  @PutMapping("delete/{id}")
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
    response.put("message", "Sản phẩm đã được xóa thành công!");
    return response;
  }
}