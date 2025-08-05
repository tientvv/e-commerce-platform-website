package com.tientvv.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MockBankService {

    // Lưu trữ các giao dịch đang chờ xử lý
    private final Map<String, MockTransaction> pendingTransactions = new HashMap<>();

    public Map<String, Object> createPaymentUrl(String orderId, Long amount, String description) {
        try {
            log.info("Creating Mock Bank payment for orderId: {}, amount: {}", orderId, amount);

            // Tạo transaction ID
            String transactionId = "TXN_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            // Tạo mock payment URL
            String paymentUrl = "http://localhost:3000/mock-bank-payment?" +
                    "transactionId=" + transactionId +
                    "&orderId=" + orderId +
                    "&amount=" + amount +
                    "&description=" + description;

            // Lưu transaction để verify sau
            MockTransaction transaction = new MockTransaction();
            transaction.setTransactionId(transactionId);
            transaction.setOrderId(orderId);
            transaction.setAmount(amount);
            transaction.setDescription(description);
            transaction.setStatus("PENDING");

            pendingTransactions.put(transactionId, transaction);

            log.info("Mock Bank payment URL created: {}", paymentUrl);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("paymentUrl", paymentUrl);
            response.put("orderCode", orderId);
            response.put("amount", amount);
            response.put("transactionId", transactionId);
            return response;

        } catch (Exception e) {
            log.error("Error creating Mock Bank payment: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi tạo thanh toán ngân hàng: " + e.getMessage());
            return errorResponse;
        }
    }

    public boolean verifyPayment(String transactionId, String orderId) {
        try {
            log.info("Verifying Mock Bank payment - transactionId: {}, orderId: {}", transactionId, orderId);

            MockTransaction transaction = pendingTransactions.get(transactionId);
            if (transaction == null) {
                log.error("Transaction not found: {}", transactionId);
                return false;
            }

            // Kiểm tra orderId có khớp không
            if (!orderId.equals(transaction.getOrderId())) {
                log.error("OrderId mismatch - expected: {}, actual: {}", transaction.getOrderId(), orderId);
                return false;
            }

            // Mock: 90% thành công, 10% thất bại
            boolean isSuccess = Math.random() > 0.1;
            
            if (isSuccess) {
                transaction.setStatus("SUCCESS");
                log.info("Mock Bank payment successful for transaction: {}", transactionId);
            } else {
                transaction.setStatus("FAILED");
                log.info("Mock Bank payment failed for transaction: {}", transactionId);
            }

            return isSuccess;

        } catch (Exception e) {
            log.error("Error verifying Mock Bank payment: ", e);
            return false;
        }
    }

    public MockTransaction getTransaction(String transactionId) {
        return pendingTransactions.get(transactionId);
    }

    public void removeTransaction(String transactionId) {
        pendingTransactions.remove(transactionId);
        log.info("Removed transaction: {}", transactionId);
    }

    // Inner class để lưu thông tin transaction
    public static class MockTransaction {
        private String transactionId;
        private String orderId;
        private Long amount;
        private String description;
        private String status;

        // Getters and Setters
        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public Long getAmount() {
            return amount;
        }

        public void setAmount(Long amount) {
            this.amount = amount;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
} 