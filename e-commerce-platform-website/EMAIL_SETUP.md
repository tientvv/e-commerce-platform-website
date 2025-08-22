# Hướng dẫn cấu hình Email cho hệ thống E-commerce

## 1. Cấu hình Gmail SMTP

### Bước 1: Bật 2-Factor Authentication
1. Đăng nhập vào tài khoản Gmail
2. Vào **Google Account Settings** > **Security**
3. Bật **2-Step Verification**

### Bước 2: Tạo App Password
1. Vào **Google Account Settings** > **Security** > **2-Step Verification**
2. Cuộn xuống **App passwords**
3. Chọn **Mail** và **Other (Custom name)**
4. Đặt tên: `E-commerce Platform`
5. Copy mật khẩu được tạo (16 ký tự)

### Bước 3: Cấu hình Environment Variables
Thêm vào file `.env`:

```env
# Email Configuration
EMAIL_USERNAME=your-email@gmail.com
EMAIL_PASSWORD=your-app-password-16-chars
```

## 2. Các loại Email được gửi

### 2.1. Email xác nhận đơn hàng
- **Khi nào**: Khi khách hàng đặt hàng thành công
- **Nội dung**: 
  - Thông tin đơn hàng
  - Chi tiết sản phẩm
  - Tổng tiền
  - Trạng thái đơn hàng

### 2.2. Email cập nhật trạng thái đơn hàng
- **Khi nào**: Khi shop cập nhật trạng thái đơn hàng
- **Nội dung**:
  - Thông báo thay đổi trạng thái
  - Thông tin đơn hàng
  - Trạng thái mới

### 2.3. Email hủy đơn hàng
- **Khi nào**: Khi đơn hàng bị hủy
- **Nội dung**:
  - Thông báo hủy đơn hàng
  - Thông tin hoàn tiền (nếu có)
  - Thông tin đơn hàng

### 2.4. Email giao hàng thành công
- **Khi nào**: Khi đơn hàng được giao thành công
- **Nội dung**:
  - Thông báo giao hàng thành công
  - Yêu cầu đánh giá sản phẩm
  - Thông tin đơn hàng

## 3. Cấu hình trong application.properties

```properties
# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${EMAIL_USERNAME}
spring.mail.password=${EMAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.ssl.trust=smtp.gmail.com
```

## 4. Testing Email

### 4.1. Test đơn hàng mới
1. Tạo đơn hàng mới
2. Kiểm tra email xác nhận được gửi

### 4.2. Test cập nhật trạng thái
1. Cập nhật trạng thái đơn hàng trong admin
2. Kiểm tra email thông báo được gửi

### 4.3. Test hủy đơn hàng
1. Hủy đơn hàng
2. Kiểm tra email hủy đơn hàng

## 5. Troubleshooting

### 5.1. Lỗi Authentication
```
Error: 535-5.7.8 Username and Password not accepted
```
**Giải pháp**: 
- Kiểm tra App Password đã tạo đúng chưa
- Đảm bảo 2-Factor Authentication đã bật

### 5.2. Lỗi Connection
```
Error: Connection refused
```
**Giải pháp**:
- Kiểm tra kết nối internet
- Kiểm tra firewall
- Thử port 465 với SSL

### 5.3. Email không được gửi
**Giải pháp**:
- Kiểm tra logs trong console
- Đảm bảo EmailService được inject đúng
- Kiểm tra email khách hàng có hợp lệ không

## 6. Cấu hình nâng cao

### 6.1. Sử dụng SMTP khác
Thay đổi cấu hình cho SMTP khác:

```properties
# Outlook/Hotmail
spring.mail.host=smtp-mail.outlook.com
spring.mail.port=587

# Yahoo
spring.mail.host=smtp.mail.yahoo.com
spring.mail.port=587

# Custom SMTP
spring.mail.host=your-smtp-server.com
spring.mail.port=587
```

### 6.2. Cấu hình SSL/TLS
```properties
spring.mail.properties.mail.smtp.ssl.enable=true
spring.mail.properties.mail.smtp.ssl.trust=*
```

### 6.3. Cấu hình timeout
```properties
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.connectiontimeout=5000
```

## 7. Monitoring và Logging

### 7.1. Log Email
Thêm vào `application.properties`:
```properties
logging.level.com.tientvv.service.EmailService=DEBUG
```

### 7.2. Kiểm tra Email Queue
- Theo dõi logs để xem email được gửi
- Kiểm tra spam folder nếu không nhận được email

## 8. Security Considerations

### 8.1. Bảo mật thông tin
- Không commit email password vào git
- Sử dụng environment variables
- Rotate App Password định kỳ

### 8.2. Rate Limiting
- Gmail có giới hạn 500 email/ngày cho tài khoản free
- Cân nhắc sử dụng Gmail Business cho production

### 8.3. Email Validation
- Validate email format trước khi gửi
- Handle bounce emails
- Implement email verification

## 9. Production Deployment

### 9.1. Email Service Provider
Cân nhắc sử dụng email service provider:
- SendGrid
- Mailgun
- Amazon SES
- Postmark

### 9.2. Email Templates
- Sử dụng Thymeleaf templates
- Implement email queue system
- Add email tracking

### 9.3. Monitoring
- Set up email delivery monitoring
- Track email open rates
- Monitor bounce rates
