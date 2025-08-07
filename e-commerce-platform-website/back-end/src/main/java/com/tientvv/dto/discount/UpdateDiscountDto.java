package com.tientvv.dto.discount;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class UpdateDiscountDto {
  @NotNull(message = "ID không được để trống")
  private UUID id;

  @NotBlank(message = "Tên giảm giá không được để trống")
  @Size(max = 255, message = "Tên giảm giá không được vượt quá 255 ký tự")
  private String name;

  private String description;

  @NotBlank(message = "Loại giảm giá không được để trống")
  @Pattern(regexp = "PERCENTAGE|FIXED", message = "Loại giảm giá phải là PERCENTAGE hoặc FIXED")
  private String discountType;

  @NotNull(message = "Giá trị giảm giá không được để trống")
  @DecimalMin(value = "0.01", message = "Giá trị giảm giá phải lớn hơn 0")
  private BigDecimal discountValue;

  @NotNull(message = "Ngày bắt đầu không được để trống")
  private OffsetDateTime startDate;

  @NotNull(message = "Ngày kết thúc không được để trống")
  private OffsetDateTime endDate;

  @DecimalMin(value = "0", message = "Giá trị đơn hàng tối thiểu phải >= 0")
  private BigDecimal minOrderValue;

  @NotBlank(message = "Loại áp dụng không được để trống")
  @Pattern(regexp = "ALL|PRODUCT|CATEGORY|VARIANT", message = "Loại áp dụng phải là ALL, PRODUCT, CATEGORY hoặc VARIANT")
  private String applicationType;

  private UUID productId;
  private UUID categoryId;
  private UUID productVariantId;
}