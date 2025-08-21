package com.tientvv.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tientvv.dto.CrudProduct.CreateProductVariantDto;
import com.tientvv.dto.CrudProduct.ProductVariantDto;
import com.tientvv.dto.CrudProduct.UpdateProductVariantDto;
import com.tientvv.model.Product;
import com.tientvv.model.ProductVariant;
import com.tientvv.repository.ProductRepository;
import com.tientvv.repository.ProductVariantRepository;

@Service
public class ProductVariantService {

  @Autowired
  private ProductVariantRepository productVariantRepository;

  @Autowired
  private ProductRepository productRepository;

  public ProductVariantDto createProductVariant(CreateProductVariantDto dto) {
    Product product = productRepository.findById(dto.getProductId())
        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));

    // Kiểm tra xem variant đã tồn tại chưa
    if (productVariantRepository.existsByProductIdAndVariantNameAndVariantValue(
        dto.getProductId(), dto.getVariantName(), dto.getVariantValue())) {
      throw new RuntimeException("Biến thể này đã tồn tại!");
    }

    ProductVariant productVariant = new ProductVariant();
    productVariant.setProduct(product);
    productVariant.setVariantName(dto.getVariantName());
    productVariant.setVariantValue(dto.getVariantValue());
    productVariant.setQuantity(dto.getQuantity());
    productVariant.setPrice(dto.getPrice());
    productVariant.setIsActive(true);

    ProductVariant savedVariant = productVariantRepository.save(productVariant);
    return convertToDto(savedVariant);
  }

  public ProductVariantDto getProductVariantById(UUID id) {
    ProductVariant productVariant = productVariantRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm!"));
    return convertToDto(productVariant);
  }

  public List<ProductVariantDto> getProductVariantsByProductId(UUID productId) {
    List<ProductVariant> variants = productVariantRepository.findByProductIdAndIsActiveTrue(productId);
    return variants.stream().map(this::convertToDto).collect(Collectors.toList());
  }

  public List<ProductVariantDto> getAllProductVariants() {
    List<ProductVariant> variants = productVariantRepository.findByIsActiveTrue();
    return variants.stream().map(this::convertToDto).collect(Collectors.toList());
  }

  public ProductVariantDto updateProductVariant(UUID id, UpdateProductVariantDto dto) {
    ProductVariant productVariant = productVariantRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm!"));

    if (dto.getVariantName() != null) {
      productVariant.setVariantName(dto.getVariantName());
    }
    if (dto.getVariantValue() != null) {
      productVariant.setVariantValue(dto.getVariantValue());
    }
    if (dto.getQuantity() != null) {
      productVariant.setQuantity(dto.getQuantity());
    }
    if (dto.getPrice() != null) {
      productVariant.setPrice(dto.getPrice());
    }
    if (dto.getIsActive() != null) {
      productVariant.setIsActive(dto.getIsActive());
    }

    ProductVariant updatedVariant = productVariantRepository.save(productVariant);
    return convertToDto(updatedVariant);
  }

  public void deleteProductVariant(UUID id) {
    ProductVariant productVariant = productVariantRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm!"));

    productVariant.setIsActive(false);
    productVariantRepository.save(productVariant);
  }

  private ProductVariantDto convertToDto(ProductVariant productVariant) {
    // Lấy ảnh chính của variant (ảnh đầu tiên nếu có)
    String imageUrl = null;
    if (productVariant.getProductImages() != null && !productVariant.getProductImages().isEmpty()) {
      imageUrl = productVariant.getProductImages().iterator().next().getImageUrl();
    }
    
    // Lấy tất cả ảnh của variant
    List<String> images = null;
    if (productVariant.getProductImages() != null && !productVariant.getProductImages().isEmpty()) {
      images = productVariant.getProductImages().stream()
          .map(image -> image.getImageUrl())
          .collect(Collectors.toList());
    }
    
    return new ProductVariantDto(
        productVariant.getId(),
        productVariant.getProduct().getId(),
        productVariant.getVariantName(),
        productVariant.getVariantValue(),
        productVariant.getQuantity(),
        productVariant.getPrice(),
        productVariant.getIsActive(),
        imageUrl,
        images);
  }
}