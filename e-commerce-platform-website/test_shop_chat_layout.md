# Test Layout Shop Chat

## Vấn đề đã sửa:
- Xóa `MenuBarView` khỏi `ShopChatView.vue` vì nó đã được render bởi layout chính `ShopView.vue`
- Sửa layout để sử dụng flexbox đúng cách
- Đảm bảo height và width hoạt động chính xác

## Cách test:

### 1. Đăng nhập shop owner
- Vào `/login`
- Đăng nhập với tài khoản có shop

### 2. Vào trang quản lý tin nhắn
- Vào `/user/shop/chat`
- Hoặc click vào "Quản lý tin nhắn" trong menu

### 3. Kiểm tra layout
- **Bên trái**: Menu "Danh mục" với các tùy chọn
- **Bên phải**: Nội dung quản lý tin nhắn
- **Không có**: Menu "Danh mục" xuất hiện trong content area

### 4. Kiểm tra chức năng
- Danh sách tin nhắn hiển thị bên trái
- Click vào một cuộc trò chuyện
- Tin nhắn hiển thị bên phải
- Có thể gửi tin nhắn trả lời

## Nếu vẫn có vấn đề:

### Kiểm tra console
- Mở Developer Tools (F12)
- Xem có lỗi JavaScript không

### Kiểm tra network
- Xem API calls có thành công không
- Kiểm tra response data

### Kiểm tra CSS
- Xem có CSS conflict không
- Kiểm tra flexbox layout
