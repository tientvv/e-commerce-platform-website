package com.tientvv.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tientvv.dto.CrudProduct.CreateProductDto;
import com.tientvv.dto.CrudProduct.ProductDto;
import com.tientvv.dto.CrudProduct.UpdateProductDto;
import com.tientvv.model.Category;
import com.tientvv.model.Product;
import com.tientvv.model.Shop;
import com.tientvv.repository.CategoryRepository;
import com.tientvv.repository.ProductRepository;
import com.tientvv.repository.ShopRepository;

@Service
public class ProductService {

  @Autowired
  private ImageUploadService imageUploadService;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private ShopRepository shopRepository;

  public List<Product> getAllProducts() {
    return productRepository.findAll();
  }

  public Product createProduct(CreateProductDto dto) throws Exception {
    Product product = new Product();
    Category category = categoryRepository.findById(dto.getCategoryId())
        .orElseThrow(() -> new RuntimeException("Category not found"));
    product.setCategory(category);
    product.setBrand(dto.getBrand() != null ? dto.getBrand() : "");
    product.setName(dto.getName());
    if (dto.getProductImage() != null && !dto.getProductImage().isEmpty()) {
      String imageUrl = imageUploadService.uploadImage(dto.getProductImage());
      product.setProductImage(imageUrl);
    }
    product.setDescription(dto.getDescription());
    Shop shop = shopRepository.findById(dto.getShopId())
        .orElseThrow(() -> new RuntimeException("Shop not found"));
    product.setIsActive(true);
    product.setShop(shop);
    return productRepository.save(product);
  }

  public Product updateProduct(UUID id, UpdateProductDto dto) throws Exception {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Product not found"));
    product.setName(dto.getName());
    product.setBrand(dto.getBrand());
    product.setDescription(dto.getDescription());
    if (dto.getCategoryId() != null) {
      Category category = categoryRepository.findById(dto.getCategoryId())
          .orElseThrow(() -> new RuntimeException("Category not found"));
      product.setCategory(category);
    }
    if (dto.getShopId() != null) {
      Shop shop = shopRepository.findById(dto.getShopId())
          .orElseThrow(() -> new RuntimeException("Shop not found"));
      product.setShop(shop);
    }
    if (dto.getProductImage() != null && !dto.getProductImage().isEmpty()) {
      String imageUrl = imageUploadService.uploadImage(dto.getProductImage());
      product.setProductImage(imageUrl);
    }
    return productRepository.save(product);
  }

  public void deleteProduct(UUID productId) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new RuntimeException("Product not found"));
    product.setIsActive(false);
    productRepository.save(product);
  }

  public List<ProductDto> getProductByShopIdAndIsActive(UUID shopId, Boolean isActive) {
    return productRepository.findByShopIdAndIsActive(shopId, isActive);
  }

  public ProductDto getProductDtoById(UUID id) {
    Product product = productRepository.findById(id).orElse(null);
    if (product == null) {
      return null;
    }
    return ProductDto.fromEntity(product);
  }
}
