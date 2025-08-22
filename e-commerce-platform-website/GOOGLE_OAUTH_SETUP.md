# Hướng dẫn cấu hình Google OAuth

## Bước 1: Tạo Google OAuth 2.0 Client ID

1. Truy cập [Google Cloud Console](https://console.cloud.google.com/)
2. Tạo project mới hoặc chọn project hiện có
3. Bật Google+ API và Google OAuth2 API
4. Vào "Credentials" > "Create Credentials" > "OAuth 2.0 Client IDs"
5. Chọn "Web application"
6. Đặt tên cho client
7. Thêm Authorized JavaScript origins:
   - `http://localhost:3000` (cho development)
   - `http://localhost:5173` (cho Vite dev server)
   - Domain production của bạn (khi deploy)
8. Thêm Authorized redirect URIs:
   - `http://localhost:3000`
   - `http://localhost:5173`
   - Domain production của bạn
9. Lưu lại Client ID và Client Secret

## Bước 2: Cấu hình Backend

Thêm các biến môi trường vào file `.env` của backend:

```env
GOOGLE_CLIENT_ID=your_google_client_id_here
GOOGLE_CLIENT_SECRET=your_google_client_secret_here
```

## Bước 3: Cấu hình Frontend

Tạo file `.env` trong thư mục `front-end`:

```env
VITE_GOOGLE_CLIENT_ID=your_google_client_id_here
```

## Bước 4: Cấu hình Database

Đảm bảo bảng `accounts` có cột `google_id`:

```sql
ALTER TABLE accounts ADD COLUMN google_id VARCHAR(255);
```

## Bước 5: Test

1. Khởi động backend: `cd back-end && mvn spring-boot:run`
2. Khởi động frontend: `cd front-end && npm run dev`
3. Truy cập trang đăng nhập/đăng ký
4. Test tính năng đăng nhập Google

## Lưu ý

- Đảm bảo domain được cấu hình đúng trong Google Cloud Console
- Client Secret chỉ được sử dụng ở backend, không bao giờ expose ra frontend
- Cập nhật domain trong Google Cloud Console khi deploy lên production
