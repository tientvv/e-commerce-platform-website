package com.tientvv.service;

import com.tientvv.dto.order.OrderDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class InvoiceService {

    public String generateInvoiceHtml(OrderDto order) {
        return generateHtmlContent(order);
    }

    public String generateInvoicePdf(OrderDto order) {
        return generateHtmlContent(order);
    }

    private String generateHtmlContent(OrderDto order) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<title>Hóa đơn ").append(order.getOrderCode()).append("</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }");
        html.append(".container { max-width: 800px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }");
        html.append(".header { text-align: center; font-size: 28px; font-weight: bold; margin-bottom: 30px; color: #2c3e50; border-bottom: 3px solid #3498db; padding-bottom: 15px; }");
        html.append(".section { margin-bottom: 25px; }");
        html.append(".section h3 { color: #2c3e50; border-bottom: 2px solid #ecf0f1; padding-bottom: 8px; margin-bottom: 15px; }");
        html.append(".info-row { display: flex; margin-bottom: 10px; align-items: center; }");
        html.append(".label { font-weight: bold; width: 200px; color: #34495e; }");
        html.append(".value { flex: 1; color: #2c3e50; }");
        html.append("table { width: 100%; border-collapse: collapse; margin: 20px 0; }");
        html.append("th, td { border: 1px solid #bdc3c7; padding: 12px; text-align: left; }");
        html.append("th { background-color: #3498db; color: white; font-weight: bold; }");
        html.append("tr:nth-child(even) { background-color: #f8f9fa; }");
        html.append("tr:hover { background-color: #e8f4f8; }");
        html.append(".total-section { margin-top: 25px; text-align: right; background-color: #ecf0f1; padding: 20px; border-radius: 5px; }");
        html.append(".total-row { display: flex; justify-content: space-between; margin-bottom: 8px; }");
        html.append(".total-row.final { font-weight: bold; font-size: 18px; color: #e74c3c; border-top: 2px solid #bdc3c7; padding-top: 10px; }");
        html.append(".footer { text-align: center; margin-top: 40px; font-style: italic; color: #7f8c8d; border-top: 1px solid #ecf0f1; padding-top: 20px; }");
        html.append("@media print { body { background-color: white; } .container { box-shadow: none; } }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        
        html.append("<div class='container'>");
        
        // Header
        html.append("<div class='header'>HÓA ĐƠN BÁN HÀNG</div>");
        
        // Thông tin khách hàng
        html.append("<div class='section'>");
        html.append("<h3>Thông tin khách hàng</h3>");
        html.append("<div class='info-row'><span class='label'>Mã đơn hàng:</span><span class='value'>").append(order.getOrderCode()).append("</span></div>");
        html.append("<div class='info-row'><span class='label'>Ngày đặt hàng:</span><span class='value'>").append(formatDate(order.getOrderDate())).append("</span></div>");
        html.append("<div class='info-row'><span class='label'>Khách hàng:</span><span class='value'>").append(order.getAccountName()).append("</span></div>");
        html.append("<div class='info-row'><span class='label'>Email:</span><span class='value'>").append(order.getAccountEmail()).append("</span></div>");
        html.append("<div class='info-row'><span class='label'>Số điện thoại:</span><span class='value'>").append(order.getAccountPhone()).append("</span></div>");
        html.append("<div class='info-row'><span class='label'>Địa chỉ giao hàng:</span><span class='value'>").append(order.getShippingAddress()).append("</span></div>");
        html.append("</div>");
        
        // Thông tin cửa hàng
        html.append("<div class='section'>");
        html.append("<h3>Thông tin cửa hàng</h3>");
        html.append("<div class='info-row'><span class='label'>Cửa hàng:</span><span class='value'>").append(order.getShopName()).append("</span></div>");
        html.append("<div class='info-row'><span class='label'>Phương thức thanh toán:</span><span class='value'>").append(order.getPaymentName()).append("</span></div>");
        html.append("<div class='info-row'><span class='label'>Phương thức giao hàng:</span><span class='value'>").append(order.getShippingMethod()).append("</span></div>");
        html.append("</div>");
        
        // Bảng sản phẩm
        html.append("<div class='section'>");
        html.append("<h3>Chi tiết sản phẩm</h3>");
        html.append("<table>");
        html.append("<thead>");
        html.append("<tr>");
        html.append("<th>STT</th>");
        html.append("<th>Tên sản phẩm</th>");
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
        html.append("</div>");
        
        // Tổng tiền
        html.append("<div class='total-section'>");
        html.append("<div class='total-row'><span>Phí vận chuyển:</span><span>").append(formatPrice(order.getShippingPrice())).append("</span></div>");
        html.append("<div class='total-row'><span>Giảm giá:</span><span>").append(formatPrice(order.getDiscountAmount())).append("</span></div>");
        html.append("<div class='total-row final'><span>TỔNG CỘNG:</span><span>").append(formatPrice(order.getTotalAmount())).append("</span></div>");
        html.append("</div>");
        
        // Footer
        html.append("<div class='footer'>");
        html.append("Cảm ơn quý khách đã mua hàng!");
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
        if (date == null) return "";
        @SuppressWarnings("deprecation")
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", new Locale("vi", "VN"));
        return date.format(formatter);
    }
}
