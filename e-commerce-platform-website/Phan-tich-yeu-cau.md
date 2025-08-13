# Phân tích yêu cầu dự án E-commerce Platform

## 1. Tổng quan dự án

Dự án E-commerce Platform là một hệ thống thương mại điện tử hoàn chỉnh, hỗ trợ đa vai trò người dùng bao gồm: Khách hàng, Chủ cửa hàng và Quản trị viên hệ thống. Hệ thống được xây dựng theo kiến trúc Full-Stack với Backend Spring Boot và Frontend Vue.js.

## 2. Yêu cầu chức năng (Functional Requirements)

### 2.1 Quản lý tài khoản người dùng

#### 2.1.1 Đăng ký tài khoản
- **Mô tả**: Cho phép người dùng tạo tài khoản mới
- **Yêu cầu**:
  - Thu thập thông tin cá nhân (họ tên, email, số điện thoại)
  - Xác thực email và số điện thoại
  - Mã hóa mật khẩu bằng BCrypt
  - Kiểm tra trùng lặp email/số điện thoại

#### 2.1.2 Đăng nhập/Đăng xuất
- **Mô tả**: Xác thực người dùng và quản lý phiên làm việc
- **Yêu cầu**:
  - Đăng nhập bằng email/username và mật khẩu
  - Lưu trữ session (30 phút timeout)
  - Đăng xuất an toàn
  - Phân quyền theo vai trò

#### 2.1.3 Quản lý thông tin cá nhân
- **Mô tả**: Cập nhật thông tin cá nhân và địa chỉ
- **Yêu cầu**:
  - Chỉnh sửa thông tin cá nhân
  - Quản lý địa chỉ giao hàng
  - Thay đổi mật khẩu
  - Upload avatar

### 2.2 Quản lý sản phẩm

#### 2.2.1 Hiển thị sản phẩm
- **Mô tả**: Hiển thị danh sách sản phẩm với các bộ lọc
- **Yêu cầu**:
  - Hiển thị sản phẩm theo danh mục
  - Tìm kiếm sản phẩm theo tên, mô tả
  - Lọc theo giá, thương hiệu, đánh giá
  - Phân trang kết quả
  - Sắp xếp theo tiêu chí khác nhau

#### 2.2.2 Chi tiết sản phẩm
- **Mô tả**: Hiển thị thông tin chi tiết sản phẩm
- **Yêu cầu**:
  - Thông tin sản phẩm đầy đủ
  - Hình ảnh sản phẩm (gallery)
  - Biến thể sản phẩm (size, color, etc.)
  - Đánh giá và bình luận
  - Sản phẩm liên quan

#### 2.2.3 Quản lý sản phẩm (Shop)
- **Mô tả**: Chủ cửa hàng quản lý sản phẩm của mình
- **Yêu cầu**:
  - Thêm/sửa/xóa sản phẩm
  - Upload nhiều hình ảnh sản phẩm
  - Quản lý biến thể sản phẩm
  - Cập nhật giá và số lượng tồn kho
  - Ẩn/hiện sản phẩm

### 2.3 Quản lý đơn hàng

#### 2.3.1 Tạo đơn hàng
- **Mô tả**: Khách hàng tạo đơn hàng từ giỏ hàng
- **Yêu cầu**:
  - Chọn sản phẩm và số lượng
  - Chọn địa chỉ giao hàng
  - Chọn phương thức thanh toán
  - Áp dụng mã giảm giá
  - Tính toán phí vận chuyển

#### 2.3.2 Theo dõi đơn hàng
- **Mô tả**: Khách hàng theo dõi trạng thái đơn hàng
- **Yêu cầu**:
  - Xem lịch sử đơn hàng
  - Theo dõi trạng thái đơn hàng
  - Xem chi tiết đơn hàng
  - Hủy đơn hàng (nếu được phép)

#### 2.3.3 Quản lý đơn hàng (Shop)
- **Mô tả**: Chủ cửa hàng quản lý đơn hàng
- **Yêu cầu**:
  - Xem danh sách đơn hàng
  - Cập nhật trạng thái đơn hàng
  - Xử lý đơn hàng (xác nhận, giao hàng)
  - Thống kê doanh thu

### 2.4 Hệ thống thanh toán

#### 2.4.1 Tích hợp cổng thanh toán
- **Mô tả**: Tích hợp nhiều cổng thanh toán
- **Yêu cầu**:
  - PayOS (cổng thanh toán Việt Nam)
  - VNPay
  - Thanh toán khi nhận hàng (COD)
  - Xử lý callback từ cổng thanh toán

#### 2.4.2 Quản lý giao dịch
- **Mô tả**: Theo dõi và quản lý giao dịch thanh toán
- **Yêu cầu**:
  - Lưu trữ thông tin giao dịch
  - Xác minh giao dịch
  - Hoàn tiền (nếu cần)
  - Báo cáo giao dịch

### 2.5 Quản lý vận chuyển

#### 2.5.1 Cấu hình vận chuyển
- **Mô tả**: Quản lý phương thức và phí vận chuyển
- **Yêu cầu**:
  - Cấu hình phương thức vận chuyển
  - Tính phí vận chuyển theo khu vực
  - Thời gian giao hàng dự kiến
  - Theo dõi đơn hàng

### 2.6 Hệ thống khuyến mãi

#### 2.6.1 Quản lý mã giảm giá
- **Mô tả**: Tạo và quản lý mã giảm giá
- **Yêu cầu**:
  - Tạo mã giảm giá với điều kiện
  - Áp dụng mã giảm giá cho đơn hàng
  - Kiểm tra hiệu lực mã giảm giá
  - Thống kê sử dụng mã giảm giá

### 2.7 Quản lý cửa hàng

#### 2.7.1 Đăng ký cửa hàng
- **Mô tả**: Cho phép người dùng đăng ký làm chủ cửa hàng
- **Yêu cầu**:
  - Thu thập thông tin cửa hàng
  - Xác thực thông tin
  - Phê duyệt cửa hàng
  - Cấp quyền quản lý

#### 2.7.2 Quản lý thông tin cửa hàng
- **Mô tả**: Chủ cửa hàng quản lý thông tin cửa hàng
- **Yêu cầu**:
  - Cập nhật thông tin cửa hàng
  - Upload logo và banner
  - Cấu hình thông tin liên hệ
  - Quản lý giờ làm việc

### 2.8 Hệ thống đánh giá và bình luận

#### 2.8.1 Đánh giá sản phẩm
- **Mô tả**: Khách hàng đánh giá sản phẩm đã mua
- **Yêu cầu**:
  - Đánh giá bằng sao (1-5)
  - Viết bình luận
  - Upload hình ảnh đánh giá
  - Chỉ đánh giá sản phẩm đã mua

### 2.9 Danh sách yêu thích

#### 2.9.1 Quản lý wishlist
- **Mô tả**: Khách hàng lưu sản phẩm yêu thích
- **Yêu cầu**:
  - Thêm/xóa sản phẩm khỏi wishlist
  - Xem danh sách yêu thích
  - Chuyển sản phẩm từ wishlist vào giỏ hàng

### 2.10 Hệ thống quản trị

#### 2.10.1 Quản lý người dùng
- **Mô tả**: Admin quản lý tất cả người dùng
- **Yêu cầu**:
  - Xem danh sách người dùng
  - Khóa/mở khóa tài khoản
  - Xem thống kê người dùng

#### 2.10.2 Quản lý cửa hàng
- **Mô tả**: Admin quản lý cửa hàng
- **Yêu cầu**:
  - Phê duyệt đăng ký cửa hàng
  - Quản lý trạng thái cửa hàng
  - Xem thống kê cửa hàng

#### 2.10.3 Quản lý danh mục
- **Mô tả**: Admin quản lý danh mục sản phẩm
- **Yêu cầu**:
  - Thêm/sửa/xóa danh mục
  - Sắp xếp thứ tự hiển thị
  - Quản lý danh mục con

#### 2.10.4 Quản lý thanh toán
- **Mô tả**: Admin quản lý cấu hình thanh toán
- **Yêu cầu**:
  - Cấu hình cổng thanh toán
  - Xem báo cáo giao dịch
  - Quản lý hoàn tiền

#### 2.10.5 Quản lý vận chuyển
- **Mô tả**: Admin quản lý cấu hình vận chuyển
- **Yêu cầu**:
  - Cấu hình phương thức vận chuyển
  - Quản lý phí vận chuyển
  - Cấu hình thời gian giao hàng

#### 2.10.6 Quản lý khuyến mãi
- **Mô tả**: Admin quản lý mã giảm giá
- **Yêu cầu**:
  - Tạo mã giảm giá toàn hệ thống
  - Quản lý hiệu lực mã giảm giá
  - Xem thống kê sử dụng

## 3. Yêu cầu phi chức năng (Non-Functional Requirements)

### 3.1 Hiệu suất (Performance)

#### 3.1.1 Thời gian phản hồi
- **Yêu cầu**: Thời gian phản hồi trung bình < 2 giây
- **Mô tả**: 
  - Tải trang chủ: < 1 giây
  - Tìm kiếm sản phẩm: < 2 giây
  - Xử lý thanh toán: < 5 giây
  - Upload hình ảnh: < 10 giây

#### 3.1.2 Khả năng chịu tải
- **Yêu cầu**: Hỗ trợ đồng thời 1000+ người dùng
- **Mô tả**:
  - Xử lý 1000+ request/giây
  - Database connection pool tối ưu
  - Caching cho dữ liệu tĩnh

### 3.2 Bảo mật (Security)

#### 3.2.1 Xác thực và phân quyền
- **Yêu cầu**: Hệ thống bảo mật đa lớp
- **Mô tả**:
  - Mã hóa mật khẩu bằng BCrypt
  - Session management an toàn
  - Phân quyền theo vai trò (RBAC)
  - JWT token cho API

#### 3.2.2 Bảo vệ dữ liệu
- **Yêu cầu**: Bảo vệ thông tin cá nhân và giao dịch
- **Mô tả**:
  - Mã hóa dữ liệu nhạy cảm
  - HTTPS cho tất cả giao tiếp
  - SQL injection prevention
  - XSS protection

#### 3.2.3 Bảo mật thanh toán
- **Yêu cầu**: Tuân thủ tiêu chuẩn bảo mật thanh toán
- **Mô tả**:
  - PCI DSS compliance
  - Mã hóa thông tin thẻ
  - Audit trail cho giao dịch
  - Fraud detection

### 3.3 Khả năng mở rộng (Scalability)

#### 3.3.1 Mở rộng theo chiều ngang
- **Yêu cầu**: Hỗ trợ tăng trưởng người dùng
- **Mô tả**:
  - Microservices architecture
  - Load balancing
  - Database sharding
  - CDN cho static content

#### 3.3.2 Mở rộng theo chiều dọc
- **Yêu cầu**: Tối ưu hóa hiệu suất hệ thống
- **Mô tả**:
  - Database optimization
  - Caching strategies
  - Code optimization
  - Resource monitoring

### 3.4 Tính khả dụng (Usability)

#### 3.4.1 Giao diện người dùng
- **Yêu cầu**: Giao diện thân thiện, dễ sử dụng
- **Mô tả**:
  - Responsive design cho mobile/desktop
  - Intuitive navigation
  - Fast loading times
  - Accessibility compliance

#### 3.4.2 Trải nghiệm người dùng
- **Yêu cầu**: UX tối ưu cho mọi đối tượng
- **Mô tả**:
  - Simple checkout process
  - Clear product information
  - Easy search and filtering
  - Helpful error messages

### 3.5 Tính sẵn sàng (Availability)

#### 3.5.1 Uptime
- **Yêu cầu**: 99.9% uptime
- **Mô tả**:
  - 24/7 system availability
  - Planned maintenance windows
  - Disaster recovery plan
  - Backup strategies

#### 3.5.2 Fault tolerance
- **Yêu cầu**: Hệ thống chịu lỗi cao
- **Mô tả**:
  - Redundant systems
  - Automatic failover
  - Data backup and recovery
  - Monitoring and alerting

### 3.6 Khả năng bảo trì (Maintainability)

#### 3.6.1 Code quality
- **Yêu cầu**: Code dễ bảo trì và mở rộng
- **Mô tả**:
  - Clean code principles
  - Comprehensive documentation
  - Unit testing coverage
  - Code review process

#### 3.6.2 System monitoring
- **Yêu cầu**: Giám sát hệ thống toàn diện
- **Mô tả**:
  - Application performance monitoring
  - Error tracking and logging
  - User behavior analytics
  - System health metrics

### 3.7 Tương thích (Compatibility)

#### 3.7.1 Trình duyệt
- **Yêu cầu**: Hỗ trợ các trình duyệt phổ biến
- **Mô tả**:
  - Chrome, Firefox, Safari, Edge
  - Mobile browsers
  - Progressive Web App (PWA)

#### 3.7.2 Thiết bị
- **Yêu cầu**: Responsive trên mọi thiết bị
- **Mô tả**:
  - Desktop, tablet, mobile
  - Touch-friendly interface
  - Adaptive layouts

### 3.8 Tuân thủ (Compliance)

#### 3.8.1 Pháp lý
- **Yêu cầu**: Tuân thủ quy định pháp luật
- **Mô tả**:
  - GDPR compliance
  - Local e-commerce regulations
  - Tax compliance
  - Consumer protection laws

#### 3.8.2 Tiêu chuẩn ngành
- **Yêu cầu**: Tuân thủ tiêu chuẩn ngành
- **Mô tả**:
  - PCI DSS for payments
  - ISO 27001 for security
  - WCAG for accessibility

## 4. Ràng buộc (Constraints)

### 4.1 Ràng buộc kỹ thuật
- Sử dụng Java 21 và Spring Boot 3.5.4
- Database: Microsoft SQL Server
- Frontend: Vue.js 3 với Vite
- Cloud storage: Cloudinary cho hình ảnh

### 4.2 Ràng buộc thời gian
- Phát triển trong 6 tháng
- MVP trong 3 tháng đầu
- Testing và deployment trong 1 tháng cuối

### 4.3 Ràng buộc ngân sách
- Sử dụng công nghệ open-source
- Cloud services với chi phí tối ưu
- Development team size phù hợp

### 4.4 Ràng buộc pháp lý
- Tuân thủ quy định về thương mại điện tử
- Bảo vệ quyền riêng tư người dùng
- Tuân thủ quy định thanh toán trực tuyến

## 5. Kết luận

Phân tích yêu cầu trên đã bao quát toàn bộ chức năng và tiêu chuẩn chất lượng cần thiết cho một hệ thống e-commerce hoàn chỉnh. Các yêu cầu được thiết kế để đảm bảo hệ thống có thể đáp ứng nhu cầu của cả ba đối tượng người dùng: khách hàng, chủ cửa hàng và quản trị viên, đồng thời đảm bảo tính bảo mật, hiệu suất và khả năng mở rộng trong tương lai.
