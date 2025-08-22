package com.tientvv.service;

import com.tientvv.dto.order.OrderDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;



    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${spring.application.name}")
    private String appName;

    public void sendOrderConfirmationEmail(OrderDto order) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(order.getAccountEmail());
            helper.setSubject("Xác nhận đơn hàng #" + order.getOrderCode());

            String htmlContent = generateOrderConfirmationEmail(order);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("Order confirmation email sent to: " + order.getAccountEmail());

        } catch (MessagingException e) {
            System.err.println("Error sending order confirmation email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendOrderStatusUpdateEmail(OrderDto order, String oldStatus, String newStatus) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(order.getAccountEmail());
            helper.setSubject("Cập nhật trạng thái đơn hàng #" + order.getOrderCode());

            String htmlContent = generateOrderStatusUpdateEmail(order, oldStatus, newStatus);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("Order status update email sent to: " + order.getAccountEmail());

        } catch (MessagingException e) {
            System.err.println("Error sending order status update email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendOrderCancellationEmail(OrderDto order) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(order.getAccountEmail());
            helper.setSubject("Đơn hàng đã bị hủy #" + order.getOrderCode());

            String htmlContent = generateOrderCancellationEmail(order);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("Order cancellation email sent to: " + order.getAccountEmail());

        } catch (MessagingException e) {
            System.err.println("Error sending order cancellation email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendOrderDeliveryEmail(OrderDto order) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(order.getAccountEmail());
            helper.setSubject("Đơn hàng đã được giao #" + order.getOrderCode());

            String htmlContent = generateOrderDeliveryEmail(order);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("Order delivery email sent to: " + order.getAccountEmail());

        } catch (MessagingException e) {
            System.err.println("Error sending order delivery email: " + e.getMessage());
            e.printStackTrace();
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
        html.append(".order-info { background-color: #ecf0f1; padding: 20px; border-radius: 8px; margin-bottom: 25px; }");
        html.append(".order-info h3 { color: #2c3e50; margin-top: 0; }");
        html.append(".info-row { display: flex; justify-content: space-between; margin-bottom: 8px; }");
        html.append(".label { font-weight: bold; color: #34495e; }");
        html.append(".value { color: #2c3e50; }");
        html.append("table { width: 100%; border-collapse: collapse; margin: 20px 0; }");
        html.append("th, td { border: 1px solid #bdc3c7; padding: 12px; text-align: left; }");
        html.append("th { background-color: #3498db; color: white; font-weight: bold; }");
        html.append("tr:nth-child(even) { background-color: #f8f9fa; }");
        html.append(".total-section { background-color: #ecf0f1; padding: 20px; border-radius: 8px; margin-top: 20px; }");
        html.append(".total-row { display: flex; justify-content: space-between; margin-bottom: 8px; }");
        html.append(".total-row.final { font-weight: bold; font-size: 18px; color: #e74c3c; border-top: 2px solid #bdc3c7; padding-top: 10px; }");
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
        html.append("<h1>🎉 Đơn hàng đã được xác nhận!</h1>");
        html.append("<p>Cảm ơn bạn đã đặt hàng tại ").append(appName).append("</p>");
        html.append("</div>");
        
        // Order Info
        html.append("<div class='order-info'>");
        html.append("<h3>📋 Thông tin đơn hàng</h3>");
        html.append("<div class='info-row'><span class='label'>Mã đơn hàng:</span><span class='value'>").append(order.getOrderCode()).append("</span></div>");
        html.append("<div class='info-row'><span class='label'>Ngày đặt hàng:</span><span class='value'>").append(formatDate(order.getOrderDate())).append("</span></div>");
        html.append("<div class='info-row'><span class='label'>Trạng thái:</span><span class='value'><span class='status-badge status-").append(getStatusClass(order.getOrderStatus())).append("'>").append(getStatusLabel(order.getOrderStatus())).append("</span></span></div>");
        html.append("<div class='info-row'><span class='label'>Phương thức thanh toán:</span><span class='value'>").append(order.getPaymentName()).append("</span></div>");
        html.append("<div class='info-row'><span class='label'>Địa chỉ giao hàng:</span><span class='value'>").append(order.getShippingAddress()).append("</span></div>");
        html.append("</div>");
        
        // Order Items
        html.append("<h3>🛍️ Chi tiết sản phẩm</h3>");
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
            
            html.append("<tr>");
            html.append("<td>").append(i + 1).append("</td>");
            html.append("<td>").append(item.getProductName()).append("</td>");
            html.append("<td>").append(item.getQuantity()).append("</td>");
            html.append("<td>").append(formatPrice(item.getProductPrice())).append("</td>");
            html.append("<td>").append(formatPrice(itemTotal)).append("</td>");
            html.append("</tr>");
        }
        
        html.append("</tbody>");
        html.append("</table>");
        
        // Total
        html.append("<div class='total-section'>");
        html.append("<div class='total-row'><span>Phí vận chuyển:</span><span>").append(formatPrice(order.getShippingPrice())).append("</span></div>");
        html.append("<div class='total-row'><span>Giảm giá:</span><span>").append(formatPrice(order.getDiscountAmount())).append("</span></div>");
        html.append("<div class='total-row final'><span>TỔNG CỘNG:</span><span>").append(formatPrice(order.getTotalAmount())).append("</span></div>");
        html.append("</div>");
        
        // Footer
        html.append("<div class='footer'>");
        html.append("<p>Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi.</p>");
        html.append("<p>Trân trọng,<br>").append(appName).append("</p>");
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
        html.append("<h1>📢 Cập nhật trạng thái đơn hàng</h1>");
        html.append("<p>Đơn hàng của bạn đã được cập nhật trạng thái</p>");
        html.append("</div>");
        
        // Status Update
        html.append("<div class='status-update'>");
        html.append("<h3>🔄 Thay đổi trạng thái</h3>");
        html.append("<p>Đơn hàng #").append(order.getOrderCode()).append(" đã chuyển từ</p>");
        html.append("<span class='status-badge status-").append(getStatusClass(oldStatus)).append("'>").append(getStatusLabel(oldStatus)).append("</span>");
        html.append("<span style='font-size: 20px; margin: 0 10px;'>→</span>");
        html.append("<span class='status-badge status-").append(getStatusClass(newStatus)).append("'>").append(getStatusLabel(newStatus)).append("</span>");
        html.append("</div>");
        
        // Order Info
        html.append("<div class='order-info'>");
        html.append("<h3>📋 Thông tin đơn hàng</h3>");
        html.append("<div class='info-row'><span class='label'>Mã đơn hàng:</span><span class='value'>").append(order.getOrderCode()).append("</span></div>");
        html.append("<div class='info-row'><span class='label'>Ngày đặt hàng:</span><span class='value'>").append(formatDate(order.getOrderDate())).append("</span></div>");
        html.append("<div class='info-row'><span class='label'>Tổng tiền:</span><span class='value'>").append(formatPrice(order.getTotalAmount())).append("</span></div>");
        html.append("<div class='info-row'><span class='label'>Địa chỉ giao hàng:</span><span class='value'>").append(order.getShippingAddress()).append("</span></div>");
        html.append("</div>");
        
        // Footer
        html.append("<div class='footer'>");
        html.append("<p>Bạn có thể theo dõi đơn hàng trong tài khoản của mình.</p>");
        html.append("<p>Trân trọng,<br>").append(appName).append("</p>");
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
        html.append("<h1>❌ Đơn hàng đã bị hủy</h1>");
        html.append("<p>Đơn hàng của bạn đã được hủy thành công</p>");
        html.append("</div>");
        
        // Cancellation Notice
        html.append("<div class='cancellation-notice'>");
        html.append("<h3>⚠️ Thông báo hủy đơn hàng</h3>");
        html.append("<p>Đơn hàng #").append(order.getOrderCode()).append(" đã được hủy.</p>");
        html.append("<p>Nếu bạn đã thanh toán, tiền sẽ được hoàn lại trong vòng 3-5 ngày làm việc.</p>");
        html.append("</div>");
        
        // Order Info
        html.append("<div class='order-info'>");
        html.append("<h3>📋 Thông tin đơn hàng</h3>");
        html.append("<div class='info-row'><span class='label'>Mã đơn hàng:</span><span class='value'>").append(order.getOrderCode()).append("</span></div>");
        html.append("<div class='info-row'><span class='label'>Ngày đặt hàng:</span><span class='value'>").append(formatDate(order.getOrderDate())).append("</span></div>");
        html.append("<div class='info-row'><span class='label'>Tổng tiền:</span><span class='value'>").append(formatPrice(order.getTotalAmount())).append("</span></div>");
        html.append("<div class='info-row'><span class='label'>Ngày hủy:</span><span class='value'>").append(formatDate(order.getCancelledDate())).append("</span></div>");
        html.append("</div>");
        
        // Footer
        html.append("<div class='footer'>");
        html.append("<p>Nếu bạn có thắc mắc, vui lòng liên hệ với chúng tôi.</p>");
        html.append("<p>Trân trọng,<br>").append(appName).append("</p>");
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
        html.append("<h1>🎉 Đơn hàng đã được giao thành công!</h1>");
        html.append("<p>Cảm ơn bạn đã mua hàng tại ").append(appName).append("</p>");
        html.append("</div>");
        
        // Delivery Notice
        html.append("<div class='delivery-notice'>");
        html.append("<h3>📦 Thông báo giao hàng</h3>");
        html.append("<p>Đơn hàng #").append(order.getOrderCode()).append(" đã được giao thành công!</p>");
        html.append("<p>Vui lòng kiểm tra và xác nhận chất lượng sản phẩm.</p>");
        html.append("</div>");
        
        // Order Info
        html.append("<div class='order-info'>");
        html.append("<h3>📋 Thông tin đơn hàng</h3>");
        html.append("<div class='info-row'><span class='label'>Mã đơn hàng:</span><span class='value'>").append(order.getOrderCode()).append("</span></div>");
        html.append("<div class='info-row'><span class='label'>Ngày đặt hàng:</span><span class='value'>").append(formatDate(order.getOrderDate())).append("</span></div>");
        html.append("<div class='info-row'><span class='label'>Ngày giao hàng:</span><span class='value'>").append(formatDate(order.getDeliveredDate())).append("</span></div>");
        html.append("<div class='info-row'><span class='label'>Tổng tiền:</span><span class='value'>").append(formatPrice(order.getTotalAmount())).append("</span></div>");
        html.append("<div class='info-row'><span class='label'>Địa chỉ giao hàng:</span><span class='value'>").append(order.getShippingAddress()).append("</span></div>");
        html.append("</div>");
        
        // Footer
        html.append("<div class='footer'>");
        html.append("<p>Hãy đánh giá sản phẩm để giúp chúng tôi cải thiện dịch vụ!</p>");
        html.append("<p>Trân trọng,<br>").append(appName).append("</p>");
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
}
