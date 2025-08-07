package com.tientvv.controller;

import com.tientvv.dto.shipping.ShippingDto;
import com.tientvv.service.ShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/shippings")
public class ShippingController {

  @Autowired
  private ShippingService shippingService;

  @GetMapping
  public ResponseEntity<Map<String, Object>> getAllActiveShippings() {
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
}