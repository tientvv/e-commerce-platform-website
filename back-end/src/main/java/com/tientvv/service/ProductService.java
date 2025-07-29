package com.tientvv.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tientvv.dto.CrudProduct.CreateProductDto;
import com.tientvv.dto.CrudProduct.ProductDto;
import com.tientvv.dto.CrudProduct.ProductDisplayDto;
import com.tientvv.dto.CrudProduct.ProductDetailDto;
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

    // Check if product has variants or images
    if (!product.getProductVariants().isEmpty()) {
      throw new RuntimeException("Không thể xóa sản phẩm có biến thể. Vui lòng xóa biến thể trước.");
    }

    if (!product.getProductImages().isEmpty()) {
      throw new RuntimeException("Không thể xóa sản phẩm có hình ảnh. Vui lòng xóa hình ảnh trước.");
    }

    // Hard delete the product
    productRepository.delete(product);
  }

  public List<ProductDto> getProductByShopIdAndIsActive(UUID shopId, Boolean isActive) {
    return productRepository.findByShopIdAndIsActive(shopId, isActive);
  }

  public List<Product> findAllByShopId(UUID shopId) {
    return productRepository.findAllByShopIdAndIsActiveTrue(shopId);
  }

  public List<Product> findAllByShopIdIncludingInactive(UUID shopId) {
    return productRepository.findAllByShopId(shopId);
  }

  public List<Product> findAllByShopIdAndIsActiveTrue(UUID shopId) {
    return productRepository.findAllByShopIdAndIsActiveTrue(shopId);
  }

  public List<Product> findAllByCategoryIdAndIsActiveTrue(UUID categoryId) {
    return productRepository.findAllByCategoryIdAndIsActiveTrue(categoryId);
  }

  public List<Product> findAllActiveProducts() {
    return productRepository.findByIsActiveTrue();
  }

  public List<ProductDisplayDto> findActiveProductsWithPricing() {
    return productRepository.findActiveProductsWithPricing();
  }

  public List<ProductDisplayDto> findActiveProductsWithPricingByCategoryId(UUID categoryId) {
    return productRepository.findActiveProductsWithPricingByCategoryId(categoryId);
  }

  public ProductDto getProductDtoById(UUID id) {
    Product product = productRepository.findById(id).orElse(null);
    if (product == null) {
      return null;
    }
    return ProductDto.fromEntity(product);
  }

  public ProductDetailDto getProductDetailById(UUID id) {
    return productRepository.findProductDetailById(id);
  }
}
