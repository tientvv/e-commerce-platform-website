# Tính năng Lọc Đơn hàng

## Tổng quan
Tính năng này cho phép người dùng lọc đơn hàng của họ theo các tiêu chí khác nhau để dễ dàng quản lý và theo dõi trạng thái đơn hàng.

## Tính năng mới

### 1. Lọc đơn hàng theo trạng thái
- **Tất cả đơn hàng**: Hiển thị tất cả đơn hàng không phân biệt trạng thái
- **Đơn hàng mới đặt**: Chỉ hiển thị đơn hàng chưa hoàn thành (trừ DELIVERED và CANCELLED)
- **Đơn hàng đã hủy**: Chỉ hiển thị đơn hàng đã bị hủy (CANCELLED)

### 2. Giao diện người dùng
- 3 nút lọc với thiết kế responsive
- Highlight nút đang được chọn
- Chuyển đổi mượt mà giữa các filter

## Thay đổi Backend

### 1. OrderController.java
```java
// Thêm endpoint mới
@GetMapping("/my-orders/filter")
public ResponseEntity<Map<String, Object>> getMyOrdersWithFilter(@RequestParam(required = false) String filter)
```

### 2. Logic lọc
```java
// Đơn hàng mới đặt
if ("new".equalsIgnoreCase(filter)) {
    filteredOrders = allOrders.stream()
        .filter(order -> !"DELIVERED".equals(order.getOrderStatus()) && 
                       !"CANCELLED".equals(order.getOrderStatus()))
        .collect(Collectors.toList());
}

// Đơn hàng đã hủy
else if ("cancelled".equalsIgnoreCase(filter)) {
    filteredOrders = allOrders.stream()
        .filter(order -> "CANCELLED".equals(order.getOrderStatus()))
        .collect(Collectors.toList());
}
```

## Thay đổi Frontend

### OrderView.vue
- Thêm 3 nút lọc trong header
- Thêm state `currentFilter` để theo dõi filter hiện tại
- Cập nhật `fetchOrders()` để sử dụng endpoint filter
- Thêm function `setFilter()` để xử lý chuyển đổi filter

## API Endpoints

### GET /api/orders/my-orders/filter
**Query Parameters:**
- `filter` (optional): "all", "new", "cancelled"

**Response:**
```json
{
  "success": true,
  "orders": [...],
  "filter": "new",
  "message": "Lấy danh sách đơn hàng thành công"
}
```

## Logic lọc

### 1. Đơn hàng mới đặt ("new")
Bao gồm các trạng thái:
- `PENDING_PROCESSING` - Chờ xử lý
- `PROCESSED` - Đã xử lý
- `READY_FOR_PICKUP` - Chờ lấy hàng
- `IN_TRANSIT` - Đang giao hàng

Loại trừ:
- `DELIVERED` - Đã giao hàng
- `CANCELLED` - Đã hủy

### 2. Đơn hàng đã hủy ("cancelled")
Chỉ bao gồm:
- `CANCELLED` - Đã hủy

### 3. Tất cả đơn hàng ("all")
Bao gồm tất cả trạng thái không phân biệt.

## Trạng thái đơn hàng

### Các trạng thái hiện có:
1. **PENDING_PROCESSING** - Chờ xử lý
2. **PROCESSED** - Đã xử lý
3. **READY_FOR_PICKUP** - Chờ lấy hàng
4. **IN_TRANSIT** - Đang giao hàng
5. **DELIVERED** - Đã giao hàng
6. **CANCELLED** - Đã hủy

## Testing

### Script test
```bash
./test-order-filter.sh
```

### Test cases
1. Lấy tất cả đơn hàng
2. Lọc đơn hàng mới đặt
3. Lọc đơn hàng đã hủy
4. Filter không hợp lệ
5. Filter rỗng
6. Filter "all"

## Tính năng bổ sung

### 1. Sắp xếp
- Đơn hàng luôn được sắp xếp theo thời gian đặt hàng mới nhất
- Không ảnh hưởng đến logic sắp xếp hiện tại

### 2. Error Handling
- Xử lý lỗi khi filter không hợp lệ
- Fallback về hiển thị tất cả đơn hàng
- Thông báo lỗi rõ ràng cho người dùng

### 3. Performance
- Lọc được thực hiện ở backend để giảm tải frontend
- Không cần tải lại toàn bộ dữ liệu khi chuyển filter

## Deployment

### 1. Backend
- Cập nhật OrderController
- Restart application
- Kiểm tra endpoint mới

### 2. Frontend
- Cập nhật OrderView.vue
- Build và deploy
- Kiểm tra UI/UX

## Monitoring

### Logs cần theo dõi
- Filter usage statistics
- API response times
- Error rates for filter endpoints

### Metrics
- Most used filter
- Filter switching frequency
- User engagement with filtered views
