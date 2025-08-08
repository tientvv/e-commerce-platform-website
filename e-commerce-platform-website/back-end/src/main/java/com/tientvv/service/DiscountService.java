package com.tientvv.service;

import com.tientvv.dto.discount.CreateDiscountDto;
import com.tientvv.dto.discount.DiscountDto;
import com.tientvv.dto.discount.UpdateDiscountDto;
import com.tientvv.model.*;
import com.tientvv.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DiscountService {

  private final DiscountRepository discountRepository;
  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final ProductVariantRepository productVariantRepository;

  public List<DiscountDto> getAllDiscounts() {
    List<Discount> discounts = discountRepository.findByIsActiveTrueOrderByStartDateDesc();
    return discounts.stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  public List<DiscountDto> getAllDiscountsIncludingInactive() {
    List<Discount> discounts = discountRepository.findAllByOrderByStartDateDesc();
    return discounts.stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  public List<DiscountDto> getActiveDiscounts() {
    List<Discount> discounts = discountRepository.findByIsActiveTrueOrderByStartDateDesc();
    return discounts.stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  public DiscountDto getDiscountById(UUID id) {
    Discount discount = discountRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy giảm giá với ID: " + id));
    return convertToDto(discount);
  }

  public DiscountDto createDiscount(CreateDiscountDto dto) {
    // Validate unique name
    if (discountRepository.existsByName(dto.getName())) {
      throw new RuntimeException("Tên giảm giá đã tồn tại: " + dto.getName());
    }

    // Validate date range
    if (dto.getEndDate().isBefore(dto.getStartDate())) {
      throw new RuntimeException("Ngày kết thúc phải sau ngày bắt đầu");
    }

    // Validate discount value based on type
    if ("PERCENTAGE".equals(dto.getDiscountType())
        && dto.getDiscountValue().compareTo(java.math.BigDecimal.valueOf(100)) > 0) {
      throw new RuntimeException("Giá trị phần trăm giảm giá không được vượt quá 100%");
    }

    Discount discount = new Discount();
    discount.setName(dto.getName());
    discount.setDescription(dto.getDescription());
    discount.setDiscountType(dto.getDiscountType());
    discount.setDiscountValue(dto.getDiscountValue());
    discount.setStartDate(dto.getStartDate());
    discount.setEndDate(dto.getEndDate());
    discount.setMinOrderValue(dto.getMinOrderValue());
    discount.setIsActive(true);

    // Set application scope
    setApplicationScope(discount, dto.getApplicationType(), dto.getProductId(), dto.getCategoryId(),
        dto.getProductVariantId());

    Discount savedDiscount = discountRepository.save(discount);
    return convertToDto(savedDiscount);
  }

  public DiscountDto updateDiscount(UpdateDiscountDto dto) {
    System.out.println("Updating discount with ID: " + dto.getId());
    System.out.println("Update DTO: " + dto.toString());

    Discount discount = discountRepository.findById(dto.getId())
        .orElseThrow(() -> new RuntimeException("Không tìm thấy giảm giá với ID: " + dto.getId()));

    // Validate unique name (exclude current discount)
    if (discountRepository.existsByNameAndIdNot(dto.getName(), dto.getId())) {
      throw new RuntimeException("Tên giảm giá đã tồn tại: " + dto.getName());
    }

    // Validate date range
    if (dto.getEndDate().isBefore(dto.getStartDate())) {
      throw new RuntimeException("Ngày kết thúc phải sau ngày bắt đầu");
    }

    // Validate discount value based on type
    if ("PERCENTAGE".equals(dto.getDiscountType())
        && dto.getDiscountValue().compareTo(java.math.BigDecimal.valueOf(100)) > 0) {
      throw new RuntimeException("Giá trị phần trăm giảm giá không được vượt quá 100%");
    }

    discount.setName(dto.getName());
    discount.setDescription(dto.getDescription());
    discount.setDiscountType(dto.getDiscountType());
    discount.setDiscountValue(dto.getDiscountValue());
    discount.setStartDate(dto.getStartDate());
    discount.setEndDate(dto.getEndDate());
    discount.setMinOrderValue(dto.getMinOrderValue());

    // Update application scope
    System.out.println("Setting application scope: " + dto.getApplicationType());
    System.out.println("Product ID: " + dto.getProductId());
    System.out.println("Category ID: " + dto.getCategoryId());
    System.out.println("Variant ID: " + dto.getProductVariantId());

    setApplicationScope(discount, dto.getApplicationType(), dto.getProductId(), dto.getCategoryId(),
        dto.getProductVariantId());

    Discount savedDiscount = discountRepository.save(discount);
    return convertToDto(savedDiscount);
  }

  public void deleteDiscount(UUID id) {
    Discount discount = discountRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy giảm giá với ID: " + id));

    // Soft delete
    discount.setIsActive(false);
    discountRepository.save(discount);
  }

  public void hardDeleteDiscount(UUID id) {
    if (!discountRepository.existsById(id)) {
      throw new RuntimeException("Không tìm thấy giảm giá với ID: " + id);
    }

    // For now, allow hard delete
    discountRepository.deleteById(id);
  }

  public List<DiscountDto> getActiveDiscountsByDate(OffsetDateTime date) {
    List<Discount> discounts = discountRepository.findActiveDiscountsByDate(date);
    return discounts.stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  private void setApplicationScope(Discount discount, String applicationType, UUID productId, UUID categoryId,
      UUID productVariantId) {
    // Reset all relationships
    discount.setProduct(null);
    discount.setCategory(null);
    discount.setProductVariant(null);

    switch (applicationType) {
      case "PRODUCT":
        if (productId != null) {
          Product product = productRepository.findById(productId)
              .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productId));
          discount.setProduct(product);
        }
        break;

      case "CATEGORY":
        if (categoryId != null) {
          Category category = categoryRepository.findById(categoryId)
              .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + categoryId));
          discount.setCategory(category);
        }
        break;

      case "VARIANT":
        if (productVariantId != null) {
          ProductVariant variant = productVariantRepository.findById(productVariantId)
              .orElseThrow(() -> new RuntimeException("Không tìm thấy variant với ID: " + productVariantId));
          discount.setProductVariant(variant);
        }
        break;

      case "ALL":
        // No specific target - applies to all
        break;

      default:
        throw new RuntimeException("Loại áp dụng không hợp lệ: " + applicationType);
    }
  }

  private DiscountDto convertToDto(Discount discount) {
    DiscountDto dto = new DiscountDto();
    dto.setId(discount.getId());
    dto.setName(discount.getName());
    dto.setDescription(discount.getDescription());
    dto.setDiscountType(discount.getDiscountType());
    dto.setDiscountValue(discount.getDiscountValue());
    dto.setStartDate(discount.getStartDate());
    dto.setEndDate(discount.getEndDate());
    dto.setMinOrderValue(discount.getMinOrderValue());
    dto.setIsActive(discount.getIsActive());

    // Determine application type based on relationships
    if (discount.getProduct() != null) {
      dto.setApplicationType("PRODUCT");
    } else if (discount.getCategory() != null) {
      dto.setApplicationType("CATEGORY");
    } else if (discount.getProductVariant() != null) {
      dto.setApplicationType("VARIANT");
    } else {
      dto.setApplicationType("ALL");
    }

    // Set related entities with names
    if (discount.getProduct() != null) {
      dto.setProductId(discount.getProduct().getId());
      dto.setProductName(discount.getProduct().getName());
    }
    if (discount.getCategory() != null) {
      dto.setCategoryId(discount.getCategory().getId());
      dto.setCategoryName(discount.getCategory().getName());
    }
    if (discount.getProductVariant() != null) {
      dto.setProductVariantId(discount.getProductVariant().getId());
      dto.setProductVariantName(
          discount.getProductVariant().getVariantName() + " - " + discount.getProductVariant().getVariantValue());
    }

    return dto;
  }

  public List<Discount> findActiveDiscountsForProduct(UUID productId) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
    
    // Sử dụng method có sẵn trong repository
    return discountRepository.findApplicableDiscountsForProduct(productId, product.getCategory().getId());
  }

  public DiscountDto validateDiscountCode(String code, Double orderValue) {
    // Tìm discount theo name (code)
    Discount discount = discountRepository.findByNameAndIsActiveTrue(code)
        .orElseThrow(() -> new RuntimeException("Mã giảm giá không tồn tại hoặc đã bị vô hiệu hóa"));

    // Kiểm tra thời gian hiệu lực
    OffsetDateTime now = OffsetDateTime.now();
    if (now.isBefore(discount.getStartDate()) || now.isAfter(discount.getEndDate())) {
      throw new RuntimeException("Mã giảm giá chưa có hiệu lực hoặc đã hết hạn");
    }

    // Kiểm tra giá trị đơn hàng tối thiểu
    if (discount.getMinOrderValue() != null && orderValue < discount.getMinOrderValue().doubleValue()) {
      throw new RuntimeException("Đơn hàng tối thiểu " + discount.getMinOrderValue() + "đ để áp dụng mã này");
    }

    return convertToDto(discount);
  }
}