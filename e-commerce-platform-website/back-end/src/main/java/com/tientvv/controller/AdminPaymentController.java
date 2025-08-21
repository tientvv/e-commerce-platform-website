package com.tientvv.controller;

import com.tientvv.dto.payment.CreatePaymentDto;
import com.tientvv.dto.payment.PaymentDto;
import com.tientvv.dto.payment.UpdatePaymentDto;
import com.tientvv.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/payments")
public class AdminPaymentController {

  @Autowired
  private PaymentService paymentService;

  @GetMapping
  public ResponseEntity<Map<String, Object>> getAllPayments() {
    try {
      List<PaymentDto> payments = paymentService.getAllPaymentsIncludingInactive();
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

  @SuppressWarnings("null")
  @PostMapping
  public ResponseEntity<Map<String, Object>> createPayment(@Valid @RequestBody CreatePaymentDto dto,
      BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      Map<String, Object> response = new HashMap<>();
      response.put("message", "Dữ liệu không hợp lệ: " + bindingResult.getFieldError().getDefaultMessage());
      return ResponseEntity.badRequest().body(response);
    }

    try {
      PaymentDto payment = paymentService.createPayment(dto);
      Map<String, Object> response = new HashMap<>();
      response.put("payment", payment);
      response.put("message", "Tạo phương thức thanh toán thành công");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> response = new HashMap<>();
      response.put("message", "Lỗi: " + e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  @SuppressWarnings("null")
  @PutMapping
  public ResponseEntity<Map<String, Object>> updatePayment(@Valid @RequestBody UpdatePaymentDto dto,
      BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      Map<String, Object> response = new HashMap<>();
      response.put("message", "Dữ liệu không hợp lệ: " + bindingResult.getFieldError().getDefaultMessage());
      return ResponseEntity.badRequest().body(response);
    }

    try {
      PaymentDto payment = paymentService.updatePayment(dto);
      Map<String, Object> response = new HashMap<>();
      response.put("payment", payment);
      response.put("message", "Cập nhật phương thức thanh toán thành công");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> response = new HashMap<>();
      response.put("message", "Lỗi: " + e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Map<String, Object>> deletePayment(@PathVariable UUID id) {
    try {
      paymentService.deletePayment(id);
      Map<String, Object> response = new HashMap<>();
      response.put("message", "Xóa phương thức thanh toán thành công");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> response = new HashMap<>();
      response.put("message", "Lỗi: " + e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }
}