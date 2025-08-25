package com.tientvv.controller;

import com.tientvv.dto.payment.PaymentDto;
import com.tientvv.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

  @Autowired
  private PaymentService paymentService;

  @GetMapping
  public ResponseEntity<Map<String, Object>> getAllActivePayments() {
    try {
      List<PaymentDto> payments = paymentService.getAllPayments();
      Map<String, Object> response = new HashMap<>();
      response.put("payments", payments);
      response.put("message", "Lấy danh sách phương thức thanh toán thành công");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> response = new HashMap<>();
      response.put("message", "Lỗi: " + e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<Map<String, Object>> getPaymentById(@PathVariable UUID id) {
    try {
      PaymentDto payment = paymentService.getPaymentById(id);
      Map<String, Object> response = new HashMap<>();
      response.put("payment", payment);
      response.put("message", "Lấy thông tin phương thức thanh toán thành công");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> response = new HashMap<>();
      response.put("message", "Lỗi: " + e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }
}