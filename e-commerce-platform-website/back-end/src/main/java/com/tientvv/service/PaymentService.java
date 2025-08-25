package com.tientvv.service;

import com.tientvv.dto.payment.CreatePaymentDto;
import com.tientvv.dto.payment.PaymentDto;
import com.tientvv.dto.payment.UpdatePaymentDto;
import com.tientvv.model.Payment;
import com.tientvv.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

  private final PaymentRepository paymentRepository;

  public List<PaymentDto> getAllPayments() {
    List<Payment> payments = paymentRepository.findByIsActiveTrueOrderByPaymentName();
    return payments.stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  public List<PaymentDto> getAllPaymentsIncludingInactive() {
    List<Payment> payments = paymentRepository.findAllByOrderByPaymentName();
    return payments.stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  public PaymentDto getPaymentById(UUID id) {
    Payment payment = paymentRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy phương thức thanh toán với ID: " + id));
    return convertToDto(payment);
  }

  public PaymentDto createPayment(CreatePaymentDto dto) {
    // Validate unique payment code
    if (paymentRepository.existsByPaymentCode(dto.getPaymentCode())) {
      throw new RuntimeException("Mã thanh toán đã tồn tại: " + dto.getPaymentCode());
    }

    Payment payment = new Payment();
    payment.setPaymentCode(dto.getPaymentCode());
    payment.setPaymentType(dto.getPaymentType());
    payment.setPaymentName(dto.getPaymentName());
    payment.setIcon(dto.getIcon());
    payment.setDescription(dto.getDescription());
    payment.setIsActive(true);

    Payment savedPayment = paymentRepository.save(payment);
    return convertToDto(savedPayment);
  }

  public PaymentDto updatePayment(UpdatePaymentDto dto) {
    Payment payment = paymentRepository.findById(dto.getId())
        .orElseThrow(() -> new RuntimeException("Không tìm thấy phương thức thanh toán với ID: " + dto.getId()));
    if (paymentRepository.existsByPaymentCodeAndIdNot(dto.getPaymentCode(), dto.getId())) {
      throw new RuntimeException("Mã thanh toán đã tồn tại: " + dto.getPaymentCode());
    }

    payment.setPaymentCode(dto.getPaymentCode());
    payment.setPaymentType(dto.getPaymentType());
    payment.setPaymentName(dto.getPaymentName());
    payment.setIcon(dto.getIcon());
    payment.setDescription(dto.getDescription());

    if (dto.getIsActive() != null) {
      payment.setIsActive(dto.getIsActive());
    }

    Payment savedPayment = paymentRepository.save(payment);
    return convertToDto(savedPayment);
  }

  public void deletePayment(UUID id) {
    Payment payment = paymentRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy phương thức thanh toán với ID: " + id));

    // Check if payment is used in orders
    if (!payment.getOrders().isEmpty()) {
      throw new RuntimeException("Không thể xóa phương thức thanh toán đã được sử dụng trong đơn hàng");
    }

    // Check if payment is used in transactions
    if (!payment.getTransactions().isEmpty()) {
      throw new RuntimeException("Không thể xóa phương thức thanh toán đã được sử dụng trong giao dịch");
    }

    paymentRepository.delete(payment);
  }

  private PaymentDto convertToDto(Payment payment) {
    PaymentDto dto = new PaymentDto();
    dto.setId(payment.getId());
    dto.setPaymentCode(payment.getPaymentCode());
    dto.setPaymentType(payment.getPaymentType());
    dto.setPaymentName(payment.getPaymentName());
    dto.setIcon(payment.getIcon());
    dto.setDescription(payment.getDescription());
    dto.setIsActive(payment.getIsActive());
    return dto;
  }
}