package com.tientvv.controller;

import com.tientvv.dto.shipping.CreateShippingDto;
import com.tientvv.dto.shipping.ShippingDto;
import com.tientvv.dto.shipping.UpdateShippingDto;
import com.tientvv.service.ShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/shippings")
public class AdminShippingController {

  @Autowired
  private ShippingService shippingService;

  @GetMapping
  public ResponseEntity<Map<String, Object>> getAllShippings() {
    try {
      List<ShippingDto> shippings = shippingService.getAllShippings();
      Map<String, Object> response = new HashMap<>();
      response.put("shippings", shippings);
      response.put("message", "Lấy danh sách vận chuyển thành công");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> response = new HashMap<>();
      response.put("message", "Lỗi: " + e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  @GetMapping("/all")
  public ResponseEntity<Map<String, Object>> getAllShippingsIncludingInactive() {
    try {
      List<ShippingDto> shippings = shippingService.getAllShippingsIncludingInactive();
      Map<String, Object> response = new HashMap<>();
      response.put("shippings", shippings);
      response.put("message", "Lấy danh sách vận chuyển thành công");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> response = new HashMap<>();
      response.put("message", "Lỗi: " + e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<Map<String, Object>> getShippingById(@PathVariable UUID id) {
    try {
      ShippingDto shipping = shippingService.getShippingById(id);
      Map<String, Object> response = new HashMap<>();
      response.put("shipping", shipping);
      response.put("message", "Lấy thông tin vận chuyển thành công");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> response = new HashMap<>();
      response.put("message", "Lỗi: " + e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  @PostMapping
  public ResponseEntity<Map<String, Object>> createShipping(@RequestBody CreateShippingDto createShippingDto) {
    try {
      ShippingDto shipping = shippingService.createShipping(createShippingDto);
      Map<String, Object> response = new HashMap<>();
      response.put("shipping", shipping);
      response.put("message", "Thêm phương thức vận chuyển thành công");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> response = new HashMap<>();
      response.put("message", "Lỗi: " + e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<Map<String, Object>> updateShipping(@PathVariable UUID id,
      @RequestBody UpdateShippingDto updateShippingDto) {
    try {
      ShippingDto shipping = shippingService.updateShipping(id, updateShippingDto);
      Map<String, Object> response = new HashMap<>();
      response.put("shipping", shipping);
      response.put("message", "Cập nhật phương thức vận chuyển thành công");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> response = new HashMap<>();
      response.put("message", "Lỗi: " + e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Map<String, Object>> deleteShipping(@PathVariable UUID id) {
    try {
      shippingService.deleteShipping(id);
      Map<String, Object> response = new HashMap<>();
      response.put("message", "Xóa phương thức vận chuyển thành công");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> response = new HashMap<>();
      response.put("message", "Lỗi: " + e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }
}