package com.tientvv.controller;

import com.tientvv.dto.discount.DiscountDto;
import com.tientvv.service.DiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/discounts")
public class DiscountController {

  @Autowired
  private DiscountService discountService;

  @GetMapping
  public ResponseEntity<Map<String, Object>> getAllActiveDiscounts() {
    try {
      List<DiscountDto> discounts = discountService.getAllDiscounts();
      Map<String, Object> response = new HashMap<>();
      response.put("discounts", discounts);
      response.put("message", "Lấy danh sách mã giảm giá thành công");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> response = new HashMap<>();
      response.put("message", "Lỗi: " + e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<Map<String, Object>> getDiscountById(@PathVariable UUID id) {
    try {
      DiscountDto discount = discountService.getDiscountById(id);
      Map<String, Object> response = new HashMap<>();
      response.put("discount", discount);
      response.put("message", "Lấy thông tin mã giảm giá thành công");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> response = new HashMap<>();
      response.put("message", "Lỗi: " + e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  @PostMapping("/validate")
  public ResponseEntity<Map<String, Object>> validateDiscountCode(@RequestBody Map<String, String> request) {
    try {
      String code = request.get("code");
      Double orderValue = Double.parseDouble(request.get("orderValue"));

      DiscountDto discount = discountService.validateDiscountCode(code, orderValue);
      Map<String, Object> response = new HashMap<>();
      response.put("discount", discount);
      response.put("message", "Mã giảm giá hợp lệ");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> response = new HashMap<>();
      response.put("message", "Lỗi: " + e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }
}