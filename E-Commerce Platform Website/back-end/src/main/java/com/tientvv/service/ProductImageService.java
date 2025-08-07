package com.tientvv.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tientvv.dto.CrudProduct.CreateProductImageDto;
import com.tientvv.dto.CrudProduct.ProductImageDto;
import com.tientvv.dto.CrudProduct.UpdateProductImageDto;
import com.tientvv.model.Product;
import com.tientvv.model.ProductImage;
import com.tientvv.model.ProductVariant;
import com.tientvv.repository.ProductImageRepository;
import com.tientvv.repository.ProductRepository;
import com.tientvv.repository.ProductVariantRepository;

@Service
public class ProductImageService {

  @Autowired
  private ProductImageRepository productImageRepository;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private ProductVariantRepository productVariantRepository;

  public ProductImageDto createProductImage(CreateProductImageDto dto) {
    System.out.println("=== SERVICE CREATE PRODUCT IMAGE ===");
    System.out.println("Finding product with ID: " + dto.getProductId());

    Product product = productRepository.findById(dto.getProductId())
        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));

    System.out.println("Found product: " + product.getName());

    ProductImage productImage = new ProductImage();
    productImage.setImageUrl(dto.getImageUrl());
    productImage.setProduct(product);

    System.out.println("Set imageUrl: " + dto.getImageUrl());

    // Nếu có productVariantId, thêm vào
    if (dto.getProductVariantId() != null) {
      System.out.println("Finding product variant with ID: " + dto.getProductVariantId());
      ProductVariant productVariant = productVariantRepository.findById(dto.getProductVariantId())
          .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm!"));
      productImage.setProductVariant(productVariant);
      System.out.println("Set product variant: " + productVariant.getVariantName());
    }

    System.out.println("Saving product image...");
    ProductImage savedImage = productImageRepository.save(productImage);
    System.out.println("Saved with ID: " + savedImage.getId());

    ProductImageDto result = convertToDto(savedImage);
    System.out.println("Converted to DTO: " + result);
    return result;
  }

  public ProductImageDto getProductImageById(UUID id) {
    ProductImage productImage = productImageRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh sản phẩm!"));
    return convertToDto(productImage);
  }

  public List<ProductImageDto> getProductImagesByProductId(UUID productId) {
    List<ProductImage> images = productImageRepository.findByProductId(productId);
    return images.stream().map(this::convertToDto).collect(Collectors.toList());
  }

  public List<ProductImageDto> getAllProductImages() {
    List<ProductImage> images = productImageRepository.findAll();
    return images.stream().map(this::convertToDto).collect(Collectors.toList());
  }

  public List<ProductImageDto> getProductImagesByShopId(UUID shopId) {
    List<ProductImage> images = productImageRepository.findByProductShopId(shopId);
    return images.stream().map(this::convertToDto).collect(Collectors.toList());
  }

  public List<ProductImageDto> getProductImagesByProductVariantId(UUID productVariantId) {
    List<ProductImage> images = productImageRepository.findByProductVariantId(productVariantId);
    return images.stream().map(this::convertToDto).collect(Collectors.toList());
  }

  public List<ProductImageDto> getProductMainImages(UUID productId) {
    List<ProductImage> images = productImageRepository.findByProductIdAndProductVariantIsNull(productId);
    return images.stream().map(this::convertToDto).collect(Collectors.toList());
  }

  public List<ProductImageDto> getProductVariantImages(UUID productId) {
    List<ProductImage> images = productImageRepository.findByProductIdAndProductVariantIsNotNull(productId);
    return images.stream().map(this::convertToDto).collect(Collectors.toList());
  }

  public void deleteProductImage(UUID id) {
    ProductImage productImage = productImageRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh sản phẩm!"));
    productImageRepository.delete(productImage);
  }

  public void deleteProductImagesByProductId(UUID productId) {
    List<ProductImage> images = productImageRepository.findByProductId(productId);
    productImageRepository.deleteAll(images);
  }

  public void deleteProductImagesByProductVariantId(UUID productVariantId) {
    List<ProductImage> images = productImageRepository.findByProductVariantId(productVariantId);
    productImageRepository.deleteAll(images);
  }

  public ProductImageDto updateProductImage(UUID id, UpdateProductImageDto dto) {
    ProductImage productImage = productImageRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh sản phẩm!"));

    // Cập nhật product
    if (dto.getProductId() != null) {
      Product product = productRepository.findById(dto.getProductId())
          .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));
      productImage.setProduct(product);
    }

    // Cập nhật product variant (có thể null)
    if (dto.getProductVariantId() != null) {
      ProductVariant productVariant = productVariantRepository.findById(dto.getProductVariantId())
          .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm!"));
      productImage.setProductVariant(productVariant);
    } else {
      productImage.setProductVariant(null);
    }

    ProductImage updatedImage = productImageRepository.save(productImage);
    return convertToDto(updatedImage);
  }

  private ProductImageDto convertToDto(ProductImage productImage) {
    return new ProductImageDto(
        productImage.getId(),
        productImage.getImageUrl(),
        productImage.getProduct().getId(),
        productImage.getProductVariant() != null ? productImage.getProductVariant().getId() : null);
  }
}