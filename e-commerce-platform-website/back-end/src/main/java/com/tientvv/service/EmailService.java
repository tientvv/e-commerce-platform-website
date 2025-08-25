package com.tientvv.service;

import com.tientvv.dto.order.OrderDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${spring.application.name}")
    private String appName;

    public void sendOrderConfirmationEmail(OrderDto order) {
        try {
            log.info("Preparing order confirmation email for order {} to {}", order.getOrderCode(), order.getAccountEmail());
            log.info("Order total amount: {}, shipping price: {}, discount amount: {}", 
                order.getTotalAmount(), order.getShippingPrice(), order.getDiscountAmount());
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(order.getAccountEmail());
            helper.setSubject("Xác nhận đơn hàng #" + order.getOrderCode());

            String htmlContent = generateOrderConfirmationEmail(order);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Order confirmation email sent successfully to: {}", order.getAccountEmail());

        } catch (MessagingException e) {
            log.error("Error sending order confirmation email to {}: {}", order.getAccountEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to send order confirmation email", e);
        } catch (Exception e) {
            log.error("Unexpected error sending order confirmation email to {}: {}", order.getAccountEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to send order confirmation email", e);
        }
    }

    public void sendOrderStatusUpdateEmail(OrderDto order, String oldStatus, String newStatus) {
        try {
            log.info("Preparing order status update email for order {} ({} -> {}) to {}", 
                order.getOrderCode(), oldStatus, newStatus, order.getAccountEmail());
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(order.getAccountEmail());
            helper.setSubject("Cập nhật trạng thái đơn hàng #" + order.getOrderCode());

            String htmlContent = generateOrderStatusUpdateEmail(order, oldStatus, newStatus);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Order status update email sent successfully to: {}", order.getAccountEmail());

        } catch (MessagingException e) {
            log.error("Error sending order status update email to {}: {}", order.getAccountEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to send order status update email", e);
        } catch (Exception e) {
            log.error("Unexpected error sending order status update email to {}: {}", order.getAccountEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to send order status update email", e);
        }
    }

    public void sendOrderCancellationEmail(OrderDto order) {
        try {
            log.info("Preparing order cancellation email for order {} to {}", order.getOrderCode(), order.getAccountEmail());
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(order.getAccountEmail());
            helper.setSubject("Đơn hàng đã bị hủy #" + order.getOrderCode());

            String htmlContent = generateOrderCancellationEmail(order);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Order cancellation email sent successfully to: {}", order.getAccountEmail());

        } catch (MessagingException e) {
            log.error("Error sending order cancellation email to {}: {}", order.getAccountEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to send order cancellation email", e);
        } catch (Exception e) {
            log.error("Unexpected error sending order cancellation email to {}: {}", order.getAccountEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to send order cancellation email", e);
        }
    }

    public void sendMultipleOrdersCancellationEmail(List<OrderDto> orders) {
        try {
            if (orders == null || orders.isEmpty()) {
                log.warn("No orders provided for multiple orders cancellation email");
                return;
            }

            OrderDto firstOrder = orders.get(0);
            log.info("Preparing multiple orders cancellation email for {} orders to {}", orders.size(), firstOrder.getAccountEmail());
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(firstOrder.getAccountEmail());
            helper.setSubject("Đơn hàng đã bị hủy");

            String htmlContent = generateMultipleOrdersCancellationEmail(orders);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Multiple orders cancellation email sent successfully to: {}", firstOrder.getAccountEmail());

        } catch (MessagingException e) {
            log.error("Error sending multiple orders cancellation email: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send multiple orders cancellation email", e);
        } catch (Exception e) {
            log.error("Unexpected error sending multiple orders cancellation email: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send multiple orders cancellation email", e);
        }
    }

    public void sendResetPasswordEmail(String email, String name, String token) {
        try {
            log.info("Preparing reset password email for user {} with token: {}", email, token);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Đặt lại mật khẩu - Mã xác nhận");

            String htmlContent = generateResetPasswordEmail(name, token);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Reset password email sent successfully to: {}", email);

        } catch (MessagingException e) {
            log.error("Error sending reset password email to {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Failed to send reset password email", e);
        } catch (Exception e) {
            log.error("Unexpected error sending reset password email to {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Failed to send reset password email", e);
        }
    }

    public void sendOrderDeliveryEmail(OrderDto order) {
        try {
            log.info("Preparing order delivery email for order {} to {}", order.getOrderCode(), order.getAccountEmail());
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(order.getAccountEmail());
            helper.setSubject("Đơn hàng đã được giao #" + order.getOrderCode());

            String htmlContent = generateOrderDeliveryEmail(order);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Order delivery email sent successfully to: {}", order.getAccountEmail());

        } catch (MessagingException e) {
            log.error("Error sending order delivery email to {}: {}", order.getAccountEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to send order delivery email", e);
        } catch (Exception e) {
            log.error("Unexpected error sending order delivery email to {}: {}", order.getAccountEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to send order delivery email", e);
        }
    }

    private String generateOrderConfirmationEmail(OrderDto order) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<title>Xác nhận đơn hàng</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }");
        html.append(".container { max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }");
        html.append(".header { text-align: center; color: #2c3e50; margin-bottom: 30px; }");
        html.append(".header h1 { color: #27ae60; margin-bottom: 10px; }");
        html.append(".logo { font-size: 24px; font-weight: bold; color: #3498db; margin-bottom: 10px; }");
        html.append(".order-info { background-color: #ecf0f1; padding: 20px; border-radius: 8px; margin-bottom: 25px; }");
        html.append(".order-info h3 { color: #2c3e50; margin-top: 0; }");
        html.append(".info-row { display: flex; justify-content: space-between; margin-bottom: 8px; }");
        html.append(".label { font-weight: bold; color: #34495e; }");
        html.append(".value { color: #2c3e50; }");
        html.append(".products-section { margin-bottom: 25px; }");
        html.append(".products-section h3 { color: #2c3e50; margin-bottom: 15px; }");
        html.append("table { width: 100%; border-collapse: collapse; margin-top: 15px; }");
        html.append("th { background-color: #34495e; color: white; padding: 12px; text-align: left; border: 1px solid #ddd; }");
        html.append("td { padding: 12px; border: 1px solid #ddd; }");
        html.append("tr:nth-child(even) { background-color: #f9f9f9; }");
        html.append(".total-section { background-color: #ecf0f1; padding: 20px; border-radius: 8px; margin-top: 20px; }");
        html.append(".total-row { display: flex; justify-content: space-between; margin-bottom: 8px; }");
        html.append(".total-row.final { font-weight: bold; font-size: 18px; color: #2c3e50; border-top: 2px solid #3498db; padding-top: 10px; margin-top: 10px; }");
        html.append(".footer { text-align: center; margin-top: 30px; color: #7f8c8d; font-style: italic; }");
        html.append(".status-badge { display: inline-block; padding: 6px 12px; border-radius: 20px; font-weight: bold; font-size: 12px; }");
        html.append(".status-pending { background-color: #f39c12; color: white; }");
        html.append(".status-processing { background-color: #3498db; color: white; }");
        html.append(".status-delivered { background-color: #27ae60; color: white; }");
        html.append(".status-cancelled { background-color: #e74c3c; color: white; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        
        html.append("<div class='container'>");
        
        // Header
        html.append("<div class='header'>");
        html.append("<div class='logo'>E-Commerce Platform</div>");
        html.append("<h1>Đơn hàng đã được xác nhận!</h1>");
        html.append("<p>Cảm ơn bạn đã đặt hàng tại <strong>E-Commerce Platform</strong></p>");
        html.append("</div>");
        
        // Order Info
        html.append("<div class='order-info'>");
        html.append("<h3>Thông tin đơn hàng</h3>");
        html.append("<div class='info-row'><span class='label'>Mã đơn hàng:</span><span class='value'>").append(order.getOrderCode()).append("</span></div>");
        html.append("<div class='info-row'><span class='label'>Ngày đặt hàng:</span><span class='value'>").append(formatDate(order.getOrderDate())).append("</span></div>");
        html.append("<div class='info-row'><span class='label'>Trạng thái:</span><span class='value'><span class='status-badge status-").append(getStatusClass(order.getOrderStatus())).append("'>").append(getStatusLabel(order.getOrderStatus())).append("</span></span></div>");
        html.append("<div class='info-row'><span class='label'>Phương thức thanh toán:</span><span class='value'>").append(order.getPaymentName()).append("</span></div>");
        html.append("<div class='info-row'><span class='label'>Địa chỉ giao hàng:</span><span class='value'>").append(order.getShippingAddress()).append("</span></div>");
        html.append("</div>");
        
        // Order Items
        html.append("<h3>Chi tiết sản phẩm</h3>");
        html.append("<table>");
        html.append("<thead>");
        html.append("<tr>");
        html.append("<th>STT</th>");
        html.append("<th>Sản phẩm</th>");
        html.append("<th>Số lượng</th>");
        html.append("<th>Đơn giá</th>");
        html.append("<th>Thành tiền</th>");
        html.append("</tr>");
        html.append("</thead>");
        html.append("<tbody>");
        
        for (int i = 0; i < order.getOrderItems().size(); i++) {
            OrderDto.OrderItemDto item = order.getOrderItems().get(i);
            BigDecimal itemTotal = item.getProductPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            
            // Tạo tên sản phẩm đầy đủ bao gồm variant
            String fullProductName = item.getProductName();
            if (item.getVariantName() != null && !item.getVariantName().isEmpty() && 
                item.getVariantValue() != null && !item.getVariantValue().isEmpty()) {
                fullProductName += " - " + item.getVariantName() + ": " + item.getVariantValue();
            }
            
            html.append("<tr>");
            html.append("<td>").append(i + 1).append("</td>");
            html.append("<td>").append(fullProductName).append("</td>");
            html.append("<td>").append(item.getQuantity()).append("</td>");
            html.append("<td>").append(formatPrice(item.getProductPrice())).append("</td>");
            html.append("<td>").append(formatPrice(itemTotal)).append("</td>");
            html.append("</tr>");
        }
        
        html.append("</tbody>");
        html.append("</table>");
        
        // Total Section - Đảm bảo hiển thị đầy đủ
        html.append("<div class='total-section'>");
        html.append("<div class='total-row'><span>Phí vận chuyển:</span><span>").append(formatPrice(order.getShippingPrice())).append("</span></div>");
        html.append("<div class='total-row'><span>Giảm giá:</span><span>").append(formatPrice(order.getDiscountAmount())).append("</span></div>");
        html.append("<div class='total-row final'><span>TỔNG CỘNG:</span><span>").append(formatPrice(order.getTotalAmount())).append("</span></div>");
        html.append("</div>");
        
        // Footer
        html.append("<div class='footer'>");
        html.append("<p>Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi.</p>");
        html.append("<p>Trân trọng,<br><strong>E-Commerce Platform</strong></p>");
        html.append("</div>");
        
        html.append("</div>");
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }

    private String generateOrderStatusUpdateEmail(OrderDto order, String oldStatus, String newStatus) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<title>Cập nhật trạng thái đơn hàng</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }");
        html.append(".container { max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }");
        html.append(".header { text-align: center; color: #2c3e50; margin-bottom: 30px; }");
        html.append(".header h1 { color: #3498db; margin-bottom: 10px; }");
        html.append(".status-update { background-color: #e8f4f8; padding: 20px; border-radius: 8px; margin-bottom: 25px; text-align: center; }");
        html.append(".status-update h3 { color: #2c3e50; margin-top: 0; }");
        html.append(".status-badge { display: inline-block; padding: 8px 16px; border-radius: 20px; font-weight: bold; font-size: 14px; margin: 0 10px; }");
        html.append(".status-pending { background-color: #f39c12; color: white; }");
        html.append(".status-processing { background-color: #3498db; color: white; }");
        html.append(".status-delivered { background-color: #27ae60; color: white; }");
        html.append(".status-cancelled { background-color: #e74c3c; color: white; }");
        html.append(".order-info { background-color: #ecf0f1; padding: 20px; border-radius: 8px; margin-bottom: 25px; }");
        html.append(".order-info h3 { color: #2c3e50; margin-top: 0; }");
        html.append(".info-row { display: flex; justify-content: space-between; margin-bottom: 8px; }");
        html.append(".label { font-weight: bold; color: #34495e; }");
        html.append(".value { color: #2c3e50; }");
        html.append(".footer { text-align: center; margin-top: 30px; color: #7f8c8d; font-style: italic; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        
        html.append("<div class='container'>");
        
        // Header
        html.append("<div class='header'>");
        html.append("<div class='logo'>E-Commerce Platform</div>");
        html.append("<h1>Cập nhật trạng thái đơn hàng</h1>");
        html.append("<p>Đơn hàng của bạn đã được cập nhật trạng thái</p>");
        html.append("</div>");
        
        // Status Update
        html.append("<div class='status-update'>");
        html.append("<h3>Thay đổi trạng thái</h3>");
        html.append("<p>Đơn hàng #").append(order.getOrderCode()).append(" đã chuyển từ</p>");
        html.append("<span class='status-badge status-").append(getStatusClass(oldStatus)).append("'>").append(getStatusLabel(oldStatus)).append("</span>");
        html.append("<span style='font-size: 20px; margin: 0 10px;'>→</span>");
        html.append("<span class='status-badge status-").append(getStatusClass(newStatus)).append("'>").append(getStatusLabel(newStatus)).append("</span>");
        html.append("</div>");
        
        // Order Info
        html.append("<div class='order-info'>");
        html.append("<h3>Thông tin đơn hàng</h3>");
        html.append("<div class='info-row'><span class='label'>Mã đơn hàng:</span><span class='value'>").append(order.getOrderCode()).append("</span></div>");
        html.append("<div class='info-row'><span class='label'>Ngày đặt hàng:</span><span class='value'>").append(formatDate(order.getOrderDate())).append("</span></div>");
        html.append("<div class='info-row'><span class='label'>Tổng tiền:</span><span class='value'>").append(formatPrice(order.getTotalAmount())).append("</span></div>");
        html.append("<div class='info-row'><span class='label'>Địa chỉ giao hàng:</span><span class='value'>").append(order.getShippingAddress()).append("</span></div>");
        html.append("</div>");
        
        // Products Table
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            html.append("<div class='products-section'>");
            html.append("<h3>Sản phẩm trong đơn hàng</h3>");
            html.append("<table style='width: 100%; border-collapse: collapse; margin-top: 15px;'>");
            html.append("<thead>");
            html.append("<tr style='background-color: #34495e; color: white;'>");
            html.append("<th style='padding: 12px; text-align: left; border: 1px solid #ddd;'>STT</th>");
            html.append("<th style='padding: 12px; text-align: left; border: 1px solid #ddd;'>Sản phẩm</th>");
            html.append("<th style='padding: 12px; text-align: center; border: 1px solid #ddd;'>Số lượng</th>");
            html.append("<th style='padding: 12px; text-align: right; border: 1px solid #ddd;'>Đơn giá</th>");
            html.append("<th style='padding: 12px; text-align: right; border: 1px solid #ddd;'>Thành tiền</th>");
            html.append("</tr>");
            html.append("</thead>");
            html.append("<tbody>");
            
            for (int i = 0; i < order.getOrderItems().size(); i++) {
                OrderDto.OrderItemDto item = order.getOrderItems().get(i);
                BigDecimal itemTotal = item.getProductPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                
                // Tạo tên sản phẩm đầy đủ bao gồm variant
                String fullProductName = item.getProductName();
                if (item.getVariantName() != null && !item.getVariantName().isEmpty() && 
                    item.getVariantValue() != null && !item.getVariantValue().isEmpty()) {
                    fullProductName += " - " + item.getVariantName() + ": " + item.getVariantValue();
                }
                
                html.append("<tr style='border-bottom: 1px solid #ddd;'>");
                html.append("<td style='padding: 12px; border: 1px solid #ddd;'>").append(i + 1).append("</td>");
                html.append("<td style='padding: 12px; border: 1px solid #ddd;'>").append(fullProductName).append("</td>");
                html.append("<td style='padding: 12px; text-align: center; border: 1px solid #ddd;'>").append(item.getQuantity()).append("</td>");
                html.append("<td style='padding: 12px; text-align: right; border: 1px solid #ddd;'>").append(formatPrice(item.getProductPrice())).append("</td>");
                html.append("<td style='padding: 12px; text-align: right; border: 1px solid #ddd;'>").append(formatPrice(itemTotal)).append("</td>");
                html.append("</tr>");
            }
            
            html.append("</tbody>");
            html.append("</table>");
            html.append("</div>");
        }
        
        // Footer
        html.append("<div class='footer'>");
        html.append("<p>Bạn có thể theo dõi đơn hàng trong tài khoản của mình.</p>");
        html.append("<p>Trân trọng,<br><strong>E-Commerce Platform</strong></p>");
        html.append("</div>");
        
        html.append("</div>");
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }

    private String generateOrderCancellationEmail(OrderDto order) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<title>Đơn hàng đã bị hủy</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }");
        html.append(".container { max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }");
        html.append(".header { text-align: center; color: #2c3e50; margin-bottom: 30px; }");
        html.append(".header h1 { color: #e74c3c; margin-bottom: 10px; }");
        html.append(".cancellation-notice { background-color: #fdf2f2; padding: 20px; border-radius: 8px; margin-bottom: 25px; text-align: center; border-left: 4px solid #e74c3c; }");
        html.append(".cancellation-notice h3 { color: #e74c3c; margin-top: 0; }");
        html.append(".order-info { background-color: #ecf0f1; padding: 20px; border-radius: 8px; margin-bottom: 25px; }");
        html.append(".logo { font-size: 24px; font-weight: bold; color: #3498db; margin-bottom: 10px; }");
        html.append(".order-info h3 { color: #2c3e50; margin-top: 0; }");
        html.append(".info-row { display: flex; justify-content: space-between; margin-bottom: 8px; }");
        html.append(".label { font-weight: bold; color: #34495e; }");
        html.append(".value { color: #2c3e50; }");
        html.append(".footer { text-align: center; margin-top: 30px; color: #7f8c8d; font-style: italic; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        
        html.append("<div class='container'>");
        
        // Header
        html.append("<div class='header'>");
        html.append("<div class='logo'>E-Commerce Platform</div>");
        html.append("<h1>Đơn hàng đã bị hủy</h1>");
        html.append("<p>Đơn hàng của bạn đã được hủy thành công</p>");
        html.append("</div>");
        
        // Cancellation Notice
        html.append("<div class='cancellation-notice'>");
        html.append("<h3>Thông báo hủy đơn hàng</h3>");
        html.append("<p>Đơn hàng #").append(order.getOrderCode()).append(" đã được hủy.</p>");
        html.append("<p>Nếu bạn đã thanh toán, tiền sẽ được hoàn lại trong vòng 3-5 ngày làm việc.</p>");
        html.append("</div>");
        
        // Order Info
        html.append("<div class='order-info'>");
        html.append("<h3>Thông tin đơn hàng</h3>");
        html.append("<div class='info-row'><span class='label'>Mã đơn hàng:</span><span class='value'>").append(order.getOrderCode()).append("</span></div>");
        html.append("<div class='info-row'><span class='label'>Ngày đặt hàng:</span><span class='value'>").append(formatDate(order.getOrderDate())).append("</span></div>");
        html.append("<div class='info-row'><span class='label'>Tổng tiền:</span><span class='value'>").append(formatPrice(order.getTotalAmount())).append("</span></div>");
        html.append("<div class='info-row'><span class='label'>Ngày hủy:</span><span class='value'>").append(formatDate(order.getCancelledDate())).append("</span></div>");
        html.append("</div>");
        
        // Products Table
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            html.append("<div class='products-section'>");
            html.append("<h3>Sản phẩm đã đặt</h3>");
            html.append("<table style='width: 100%; border-collapse: collapse; margin-top: 15px;'>");
            html.append("<thead>");
            html.append("<tr style='background-color: #34495e; color: white;'>");
            html.append("<th style='padding: 12px; text-align: left; border: 1px solid #ddd;'>STT</th>");
            html.append("<th style='padding: 12px; text-align: left; border: 1px solid #ddd;'>Sản phẩm</th>");
            html.append("<th style='padding: 12px; text-align: center; border: 1px solid #ddd;'>Số lượng</th>");
            html.append("<th style='padding: 12px; text-align: right; border: 1px solid #ddd;'>Đơn giá</th>");
            html.append("<th style='padding: 12px; text-align: right; border: 1px solid #ddd;'>Thành tiền</th>");
            html.append("</tr>");
            html.append("</thead>");
            html.append("<tbody>");
            
            for (int i = 0; i < order.getOrderItems().size(); i++) {
                OrderDto.OrderItemDto item = order.getOrderItems().get(i);
                BigDecimal itemTotal = item.getProductPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                
                // Tạo tên sản phẩm đầy đủ bao gồm variant
                String fullProductName = item.getProductName();
                if (item.getVariantName() != null && !item.getVariantName().isEmpty() && 
                    item.getVariantValue() != null && !item.getVariantValue().isEmpty()) {
                    fullProductName += " - " + item.getVariantName() + ": " + item.getVariantValue();
                }
                
                html.append("<tr style='border-bottom: 1px solid #ddd;'>");
                html.append("<td style='padding: 12px; border: 1px solid #ddd;'>").append(i + 1).append("</td>");
                html.append("<td style='padding: 12px; border: 1px solid #ddd;'>").append(fullProductName).append("</td>");
                html.append("<td style='padding: 12px; text-align: center; border: 1px solid #ddd;'>").append(item.getQuantity()).append("</td>");
                html.append("<td style='padding: 12px; text-align: right; border: 1px solid #ddd;'>").append(formatPrice(item.getProductPrice())).append("</td>");
                html.append("<td style='padding: 12px; text-align: right; border: 1px solid #ddd;'>").append(formatPrice(itemTotal)).append("</td>");
                html.append("</tr>");
            }
            
            html.append("</tbody>");
            html.append("</table>");
            html.append("</div>");
        }
        
        // Footer
        html.append("<div class='footer'>");
        html.append("<p>Nếu bạn có thắc mắc, vui lòng liên hệ với chúng tôi.</p>");
        html.append("<p>Trân trọng,<br><strong>E-Commerce Platform</strong></p>");
        html.append("</div>");
        
        html.append("</div>");
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }

    private String generateOrderDeliveryEmail(OrderDto order) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<title>Đơn hàng đã được giao</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }");
        html.append(".container { max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }");
        html.append(".header { text-align: center; color: #2c3e50; margin-bottom: 30px; }");
        html.append(".header h1 { color: #27ae60; margin-bottom: 10px; }");
        html.append(".delivery-notice { background-color: #e8f8f5; padding: 20px; border-radius: 8px; margin-bottom: 25px; text-align: center; border-left: 4px solid #27ae60; }");
        html.append(".delivery-notice h3 { color: #27ae60; margin-top: 0; }");
        html.append(".order-info { background-color: #ecf0f1; padding: 20px; border-radius: 8px; margin-bottom: 25px; }");
        html.append(".logo { font-size: 24px; font-weight: bold; color: #3498db; margin-bottom: 10px; }");
        html.append(".order-info h3 { color: #2c3e50; margin-top: 0; }");
        html.append(".info-row { display: flex; justify-content: space-between; margin-bottom: 8px; }");
        html.append(".label { font-weight: bold; color: #34495e; }");
        html.append(".value { color: #2c3e50; }");
        html.append(".footer { text-align: center; margin-top: 30px; color: #7f8c8d; font-style: italic; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        
        html.append("<div class='container'>");
        
        // Header
        html.append("<div class='header'>");
        html.append("<div class='logo'>E-Commerce Platform</div>");
        html.append("<h1>Đơn hàng đã được giao thành công!</h1>");
        html.append("<p>Cảm ơn bạn đã mua hàng tại <strong>E-Commerce Platform</strong></p>");
        html.append("</div>");
        
        // Delivery Notice
        html.append("<div class='delivery-notice'>");
        html.append("<h3>Thông báo giao hàng</h3>");
        html.append("<p>Đơn hàng #").append(order.getOrderCode()).append(" đã được giao thành công!</p>");
        html.append("<p>Vui lòng kiểm tra và xác nhận chất lượng sản phẩm.</p>");
        html.append("</div>");
        
        // Order Info
        html.append("<div class='order-info'>");
        html.append("<h3>Thông tin đơn hàng</h3>");
        html.append("<div class='info-row'><span class='label'>Mã đơn hàng:</span><span class='value'>").append(order.getOrderCode()).append("</span></div>");
        html.append("<div class='info-row'><span class='label'>Ngày đặt hàng:</span><span class='value'>").append(formatDate(order.getOrderDate())).append("</span></div>");
        html.append("<div class='info-row'><span class='label'>Ngày giao hàng:</span><span class='value'>").append(formatDate(order.getDeliveredDate())).append("</span></div>");
        html.append("<div class='info-row'><span class='label'>Tổng tiền:</span><span class='value'>").append(formatPrice(order.getTotalAmount())).append("</span></div>");
        html.append("<div class='info-row'><span class='label'>Địa chỉ giao hàng:</span><span class='value'>").append(order.getShippingAddress()).append("</span></div>");
        html.append("</div>");
        
        // Products Table
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            html.append("<div class='products-section'>");
            html.append("<h3>Sản phẩm đã giao</h3>");
            html.append("<table style='width: 100%; border-collapse: collapse; margin-top: 15px;'>");
            html.append("<thead>");
            html.append("<tr style='background-color: #34495e; color: white;'>");
            html.append("<th style='padding: 12px; text-align: left; border: 1px solid #ddd;'>STT</th>");
            html.append("<th style='padding: 12px; text-align: left; border: 1px solid #ddd;'>Sản phẩm</th>");
            html.append("<th style='padding: 12px; text-align: center; border: 1px solid #ddd;'>Số lượng</th>");
            html.append("<th style='padding: 12px; text-align: right; border: 1px solid #ddd;'>Đơn giá</th>");
            html.append("<th style='padding: 12px; text-align: right; border: 1px solid #ddd;'>Thành tiền</th>");
            html.append("</tr>");
            html.append("</thead>");
            html.append("<tbody>");
            
            for (int i = 0; i < order.getOrderItems().size(); i++) {
                OrderDto.OrderItemDto item = order.getOrderItems().get(i);
                BigDecimal itemTotal = item.getProductPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                
                // Tạo tên sản phẩm đầy đủ bao gồm variant
                String fullProductName = item.getProductName();
                if (item.getVariantName() != null && !item.getVariantName().isEmpty() && 
                    item.getVariantValue() != null && !item.getVariantValue().isEmpty()) {
                    fullProductName += " - " + item.getVariantName() + ": " + item.getVariantValue();
                }
                
                html.append("<tr style='border-bottom: 1px solid #ddd;'>");
                html.append("<td style='padding: 12px; border: 1px solid #ddd;'>").append(i + 1).append("</td>");
                html.append("<td style='padding: 12px; border: 1px solid #ddd;'>").append(fullProductName).append("</td>");
                html.append("<td style='padding: 12px; text-align: center; border: 1px solid #ddd;'>").append(item.getQuantity()).append("</td>");
                html.append("<td style='padding: 12px; text-align: right; border: 1px solid #ddd;'>").append(formatPrice(item.getProductPrice())).append("</td>");
                html.append("<td style='padding: 12px; text-align: right; border: 1px solid #ddd;'>").append(formatPrice(itemTotal)).append("</td>");
                html.append("</tr>");
            }
            
            html.append("</tbody>");
            html.append("</table>");
            html.append("</div>");
        }
        
        // Footer
        html.append("<div class='footer'>");
        html.append("<p>Hãy đánh giá sản phẩm để giúp chúng tôi cải thiện dịch vụ!</p>");
        html.append("<p>Trân trọng,<br><strong>E-Commerce Platform</strong></p>");
        html.append("</div>");
        
        html.append("</div>");
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) return "0 VNĐ";
        return String.format("%,.0f VNĐ", price);
    }

    private String formatDate(java.time.OffsetDateTime date) {
        if (date == null) return "N/A";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("vi-VN"));
        return date.format(formatter);
    }

    private String getStatusLabel(String status) {
        switch (status) {
            case "PENDING_PROCESSING": return "Chờ xử lý";
            case "PROCESSED": return "Đã xử lý";
            case "READY_FOR_PICKUP": return "Chờ lấy hàng";
            case "IN_TRANSIT": return "Đang giao hàng";
            case "DELIVERED": return "Đã giao hàng";
            case "CANCELLED": return "Đã hủy";
            default: return status;
        }
    }

    private String getStatusClass(String status) {
        switch (status) {
            case "PENDING_PROCESSING": return "pending";
            case "PROCESSED": return "processing";
            case "READY_FOR_PICKUP": return "processing";
            case "IN_TRANSIT": return "processing";
            case "DELIVERED": return "delivered";
            case "CANCELLED": return "cancelled";
            default: return "pending";
        }
    }

    private String generateMultipleOrdersCancellationEmail(List<OrderDto> orders) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<title>Đơn hàng đã bị hủy</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }");
        html.append(".container { max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }");
        html.append(".header { text-align: center; color: #2c3e50; margin-bottom: 30px; }");
        html.append(".header h1 { color: #e74c3c; margin-bottom: 10px; }");
        html.append(".cancellation-notice { background-color: #fdf2f2; padding: 20px; border-radius: 8px; margin-bottom: 25px; text-align: center; border-left: 4px solid #e74c3c; }");
        html.append(".cancellation-notice h3 { color: #e74c3c; margin-top: 0; }");
        html.append(".order-info { background-color: #ecf0f1; padding: 20px; border-radius: 8px; margin-bottom: 25px; }");
        html.append(".logo { font-size: 24px; font-weight: bold; color: #3498db; margin-bottom: 10px; }");
        html.append(".order-info h3 { color: #2c3e50; margin-top: 0; }");
        html.append(".info-row { display: flex; justify-content: space-between; margin-bottom: 8px; }");
        html.append(".label { font-weight: bold; color: #34495e; }");
        html.append(".value { color: #2c3e50; }");
        html.append(".footer { text-align: center; margin-top: 30px; color: #7f8c8d; font-style: italic; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        
        html.append("<div class='container'>");
        
        // Header
        html.append("<div class='header'>");
        html.append("<div class='logo'>E-Commerce Platform</div>");
        html.append("<h1>Đơn hàng đã bị hủy</h1>");
        html.append("<p>Đơn hàng của bạn đã được hủy thành công</p>");
        html.append("</div>");
        
        // Cancellation Notice
        html.append("<div class='cancellation-notice'>");
        html.append("<h3>Thông báo hủy đơn hàng</h3>");
        html.append("<p>Đơn hàng #").append(orders.get(0).getOrderCode()).append(" đã được hủy.</p>");
        html.append("<p>Nếu bạn đã thanh toán, tiền sẽ được hoàn lại trong vòng 3-5 ngày làm việc.</p>");
        html.append("</div>");
        
        // Order Info
        html.append("<div class='order-info'>");
        html.append("<h3>Thông tin đơn hàng</h3>");
        html.append("<div class='info-row'><span class='label'>Mã đơn hàng:</span><span class='value'>").append(orders.get(0).getOrderCode()).append("</span></div>");
        html.append("<div class='info-row'><span class='label'>Ngày đặt hàng:</span><span class='value'>").append(formatDate(orders.get(0).getOrderDate())).append("</span></div>");
        
        // Tính tổng tiền của tất cả đơn hàng
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderDto order : orders) {
            if (order.getTotalAmount() != null) {
                totalAmount = totalAmount.add(order.getTotalAmount());
            }
        }
        html.append("<div class='info-row'><span class='label'>Tổng tiền:</span><span class='value'>").append(formatPrice(totalAmount)).append("</span></div>");
        html.append("<div class='info-row'><span class='label'>Ngày hủy:</span><span class='value'>").append(formatDate(orders.get(0).getCancelledDate())).append("</span></div>");
        html.append("</div>");
        
        // Products Table - Gộp tất cả sản phẩm từ tất cả đơn hàng
        html.append("<div class='products-section'>");
        html.append("<h3>Sản phẩm đã đặt</h3>");
        html.append("<table style='width: 100%; border-collapse: collapse; margin-top: 15px;'>");
        html.append("<thead>");
        html.append("<tr style='background-color: #34495e; color: white;'>");
        html.append("<th style='padding: 12px; text-align: left; border: 1px solid #ddd;'>STT</th>");
        html.append("<th style='padding: 12px; text-align: left; border: 1px solid #ddd;'>Sản phẩm</th>");
        html.append("<th style='padding: 12px; text-align: center; border: 1px solid #ddd;'>Số lượng</th>");
        html.append("<th style='padding: 12px; text-align: right; border: 1px solid #ddd;'>Đơn giá</th>");
        html.append("<th style='padding: 12px; text-align: right; border: 1px solid #ddd;'>Thành tiền</th>");
        html.append("</tr>");
        html.append("</thead>");
        html.append("<tbody>");
        
        int itemIndex = 1;
        log.info("Generating multiple orders cancellation email for {} orders", orders.size());
        
        for (OrderDto order : orders) {
            log.info("Processing order {} with {} items", order.getOrderCode(), 
                order.getOrderItems() != null ? order.getOrderItems().size() : 0);
            
            if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
                for (OrderDto.OrderItemDto item : order.getOrderItems()) {
                    BigDecimal itemTotal = item.getProductPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    
                    // Tạo tên sản phẩm đầy đủ bao gồm variant
                    String fullProductName = item.getProductName();
                    if (item.getVariantName() != null && !item.getVariantName().isEmpty() && 
                        item.getVariantValue() != null && !item.getVariantValue().isEmpty()) {
                        fullProductName += " - " + item.getVariantName() + ": " + item.getVariantValue();
                    }
                    
                    log.info("Adding product {} (STT: {}) from order {}", fullProductName, itemIndex, order.getOrderCode());
                    
                    html.append("<tr style='border-bottom: 1px solid #ddd;'>");
                    html.append("<td style='padding: 12px; border: 1px solid #ddd;'>").append(itemIndex++).append("</td>");
                    html.append("<td style='padding: 12px; border: 1px solid #ddd;'>").append(fullProductName).append("</td>");
                    html.append("<td style='padding: 12px; text-align: center; border: 1px solid #ddd;'>").append(item.getQuantity()).append("</td>");
                    html.append("<td style='padding: 12px; text-align: right; border: 1px solid #ddd;'>").append(formatPrice(item.getProductPrice())).append("</td>");
                    html.append("<td style='padding: 12px; text-align: right; border: 1px solid #ddd;'>").append(formatPrice(itemTotal)).append("</td>");
                    html.append("</tr>");
                }
            }
        }
        
        log.info("Total products added to email: {}", itemIndex - 1);
        
        html.append("</tbody>");
        html.append("</table>");
        html.append("</div>");
        
        // Footer
        html.append("<div class='footer'>");
        html.append("<p>Nếu bạn có thắc mắc, vui lòng liên hệ với chúng tôi.</p>");
        html.append("<p>Trân trọng,<br><strong>E-Commerce Platform</strong></p>");
        html.append("</div>");
        
        html.append("</div>");
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }

    private String generateResetPasswordEmail(String name, String token) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<title>Đặt lại mật khẩu</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }");
        html.append(".container { max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }");
        html.append(".header { text-align: center; color: #2c3e50; margin-bottom: 30px; }");
        html.append(".header h1 { color: #3498db; margin-bottom: 10px; }");
        html.append(".reset-password-notice { background-color: #e8f4f8; padding: 20px; border-radius: 8px; margin-bottom: 25px; text-align: center; border-left: 4px solid #3498db; }");
        html.append(".reset-password-notice h3 { color: #2c3e50; margin-top: 0; }");
        html.append(".token-box { background-color: #f8f9fa; border: 2px solid #3498db; border-radius: 8px; padding: 20px; margin: 20px 0; text-align: center; }");
        html.append(".token-code { font-size: 32px; font-weight: bold; color: #3498db; letter-spacing: 5px; margin: 10px 0; }");
        html.append(".logo { font-size: 24px; font-weight: bold; color: #3498db; margin-bottom: 10px; }");
        html.append(".footer { text-align: center; margin-top: 30px; color: #7f8c8d; font-style: italic; }");
        html.append(".warning { background-color: #fff3cd; border: 1px solid #ffeaa7; border-radius: 5px; padding: 15px; margin: 20px 0; color: #856404; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        
        html.append("<div class='container'>");
        
        // Header
        html.append("<div class='header'>");
        html.append("<div class='logo'>E-Commerce Platform</div>");
        html.append("<h1>Đặt lại mật khẩu</h1>");
        html.append("<p>Xin chào ").append(name != null ? name : "Người dùng").append(",</p>");
        html.append("</div>");
        
        // Reset Password Notice
        html.append("<div class='reset-password-notice'>");
        html.append("<h3>Mã xác nhận đặt lại mật khẩu</h3>");
        html.append("<p>Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản của mình.</p>");
        html.append("<p>Vui lòng sử dụng mã xác nhận bên dưới để hoàn tất quá trình đặt lại mật khẩu.</p>");
        html.append("</div>");
        
        // Token Box
        html.append("<div class='token-box'>");
        html.append("<h3>Mã xác nhận của bạn:</h3>");
        html.append("<div class='token-code'>").append(token).append("</div>");
        html.append("<p><strong>Mã này sẽ hết hạn sau 10 phút</strong></p>");
        html.append("</div>");
        
        // Warning
        html.append("<div class='warning'>");
        html.append("<strong>Lưu ý:</strong>");
        html.append("<ul style='margin: 10px 0; padding-left: 20px;'>");
        html.append("<li>Không chia sẻ mã này với bất kỳ ai</li>");
        html.append("<li>Mã chỉ có hiệu lực trong 10 phút</li>");
        html.append("<li>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này</li>");
        html.append("</ul>");
        html.append("</div>");
        
        // Footer
        html.append("<div class='footer'>");
        html.append("<p>Trân trọng,<br><strong>E-Commerce Platform</strong></p>");
        html.append("</div>");
        
        html.append("</div>");
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }
}
