package com.tientvv.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tientvv.dto.CrudProduct.CreateProductDto;
import com.tientvv.dto.CrudProduct.ProductDto;
import com.tientvv.dto.CrudProduct.ProductDisplayDto;
import com.tientvv.dto.CrudProduct.ProductDetailDto;
import com.tientvv.dto.CrudProduct.UpdateProductDto;
import com.tientvv.model.Category;
import com.tientvv.model.Discount;
import com.tientvv.model.Product;
import com.tientvv.model.Shop;
import com.tientvv.repository.CategoryRepository;
import com.tientvv.repository.ProductRepository;
import com.tientvv.repository.ShopRepository;
import com.tientvv.specification.ProductSpecification;

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

  @Autowired
  private DiscountService discountService;

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

    // Soft delete - set isActive to false instead of hard delete
    product.setIsActive(false);
    productRepository.save(product);
  }

  public void restoreProduct(UUID productId) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new RuntimeException("Product not found"));

    // Restore product - set isActive to true
    product.setIsActive(true);
    productRepository.save(product);
  }

  public List<ProductDto> getProductByShopIdAndIsActive(UUID shopId, Boolean isActive) {
    return productRepository.findByShopIdAndIsActive(shopId, isActive);
  }

  public List<Product> findAllByShopId(UUID shopId) {
    return productRepository.findAllByShopId(shopId);
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

  public List<ProductDisplayDto> findActiveProductsWithActiveDiscounts() {
    return productRepository.findActiveProductsWithActiveDiscounts();
  }

  public List<ProductDisplayDto> findProductsWithProductSpecificDiscounts() {
    return productRepository.findActiveProductsWithProductSpecificDiscounts();
  }

  public List<ProductDisplayDto> findProductsWithDiscountsSimple() {
    return productRepository.findActiveProductsWithDiscountsSimple();
  }

  public List<ProductDisplayDto> findProductsWithBestDiscounts() {
    List<ProductDisplayDto> productsWithDiscounts = productRepository.findActiveProductsWithActiveDiscounts();
    
    // Sắp xếp theo mức độ ưu tiên discount
    return productsWithDiscounts.stream()
        .sorted((p1, p2) -> {
          // Ưu tiên theo giá trị discount (cao nhất trước)
          BigDecimal discount1 = p1.getDiscountPercentage() != null && p1.getDiscountPercentage() > 0 
              ? new BigDecimal(p1.getDiscountPercentage())
              : p1.getDiscountAmount() != null ? new BigDecimal(p1.getDiscountAmount()) : BigDecimal.ZERO;
          
          BigDecimal discount2 = p2.getDiscountPercentage() != null && p2.getDiscountPercentage() > 0 
              ? new BigDecimal(p2.getDiscountPercentage())
              : p2.getDiscountAmount() != null ? new BigDecimal(p2.getDiscountAmount()) : BigDecimal.ZERO;
          
          return discount2.compareTo(discount1); // Giảm dần
        })
        .collect(Collectors.toList());
  }

  public List<ProductDisplayDto> getSuggestedProducts() {
      // Lấy tất cả sản phẩm active có discount
      List<ProductDisplayDto> allProducts = findActiveProductsWithActiveDiscounts();
      
      // Nếu không đủ sản phẩm có discount, thêm sản phẩm thường
      if (allProducts.size() < 36) {
          List<ProductDisplayDto> regularProducts = findActiveProductsWithPricing();
          // Lọc ra những sản phẩm không có trong danh sách discount
          List<ProductDisplayDto> additionalProducts = regularProducts.stream()
              .filter(regular -> allProducts.stream()
                  .noneMatch(discount -> discount.getId().equals(regular.getId())))
              .limit(36 - allProducts.size())
              .collect(Collectors.toList());
          allProducts.addAll(additionalProducts);
      }
      
      // Tạo seed dựa trên thời gian hiện tại, chia cho 30 phút (1800000ms)
      long currentTime = System.currentTimeMillis();
      long timeSlot = currentTime / (30 * 60 * 1000); // 30 phút
      
      // Sử dụng timeSlot làm seed để đảm bảo cùng 1 kết quả trong 30 phút
      Random random = new Random(timeSlot);
      
      // Shuffle với seed cố định
      Collections.shuffle(allProducts, random);
      
      // Trả về tối đa 36 sản phẩm
      return allProducts.stream()
              .limit(36)
              .collect(Collectors.toList());
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

  public List<ProductDisplayDto> searchProducts(String query) {
    try {
      System.out.println("Searching for query: " + query);
      
      // Sử dụng Specification để search
      List<Product> products = productRepository.findAll(ProductSpecification.searchProducts(query));
      System.out.println("Raw products found: " + products.size());
      
      // Convert sang DTO
      List<ProductDisplayDto> results = products.stream()
        .map(this::convertToProductDisplayDto)
        .toList();
      
      System.out.println("Converted to DTO: " + results.size() + " products");
      return results;
    } catch (Exception e) {
      System.err.println("Error in searchProducts: " + e.getMessage());
      e.printStackTrace();
      return new ArrayList<>();
    }
  }

  public ProductDisplayDto convertToProductDisplayDto(Product product) {
    return new ProductDisplayDto() {
      @Override
      public UUID getId() { return product.getId(); }
      
      @Override
      public String getName() { return product.getName(); }
      
      @Override
      public String getBrand() { return product.getBrand(); }
      
      @Override
      public String getDescription() { return product.getDescription(); }
      
      @Override
      public String getProductImage() { return product.getProductImage(); }
      
      @Override
      public Boolean getIsActive() { return product.getIsActive(); }
      
      @Override
      public String getCategoryName() { return product.getCategory().getName(); }
      
      @Override
      public UUID getCategoryId() { return product.getCategory().getId(); }
      
      @Override
      public BigDecimal getMinPrice() { 
        if (product.getProductVariants() != null && !product.getProductVariants().isEmpty()) {
          return product.getProductVariants().stream()
            .filter(pv -> pv.getIsActive())
            .map(pv -> pv.getPrice())
            .min(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
        }
        // Nếu không có variants, trả về 0 (sẽ hiển thị "Liên hệ")
        return BigDecimal.ZERO;
      }
      
      @Override
      public BigDecimal getMaxPrice() { 
        if (product.getProductVariants() != null && !product.getProductVariants().isEmpty()) {
          return product.getProductVariants().stream()
            .filter(pv -> pv.getIsActive())
            .map(pv -> pv.getPrice())
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
        }
        // Nếu không có variants, trả về 0 (sẽ hiển thị "Liên hệ")
        return BigDecimal.ZERO;
      }
      
      @Override
      public BigDecimal getOriginalPrice() { return getMinPrice(); }
      
      @Override
      public Integer getDiscountPercentage() { 
        try {
          List<Discount> activeDiscounts = discountService.findActiveDiscountsForProduct(product.getId());
          if (!activeDiscounts.isEmpty()) {
            Discount bestDiscount = activeDiscounts.stream()
              .max((d1, d2) -> d1.getDiscountValue().compareTo(d2.getDiscountValue()))
              .orElse(null);
            if (bestDiscount != null && "PERCENTAGE".equals(bestDiscount.getDiscountType())) {
              return bestDiscount.getDiscountValue().intValue();
            }
          }
        } catch (Exception e) {
          System.err.println("Error getting discount percentage: " + e.getMessage());
        }
        return 0; 
      }
      
      @Override
      public Integer getDiscountAmount() { 
        try {
          List<Discount> activeDiscounts = discountService.findActiveDiscountsForProduct(product.getId());
          if (!activeDiscounts.isEmpty()) {
            Discount bestDiscount = activeDiscounts.stream()
              .max((d1, d2) -> d1.getDiscountValue().compareTo(d2.getDiscountValue()))
              .orElse(null);
            if (bestDiscount != null && "FIXED".equals(bestDiscount.getDiscountType())) {
              return bestDiscount.getDiscountValue().intValue();
            }
          }
        } catch (Exception e) {
          System.err.println("Error getting discount amount: " + e.getMessage());
        }
        return 0; 
      }
      
      @Override
      public String getDiscountType() { 
        try {
          List<Discount> activeDiscounts = discountService.findActiveDiscountsForProduct(product.getId());
          if (!activeDiscounts.isEmpty()) {
            Discount bestDiscount = activeDiscounts.stream()
              .max((d1, d2) -> d1.getDiscountValue().compareTo(d2.getDiscountValue()))
              .orElse(null);
            return bestDiscount != null ? bestDiscount.getDiscountType() : null;
          }
        } catch (Exception e) {
          System.err.println("Error getting discount type: " + e.getMessage());
        }
        return null; 
      }
      
      @Override
      public String getDiscountName() { 
        try {
          List<Discount> activeDiscounts = discountService.findActiveDiscountsForProduct(product.getId());
          if (!activeDiscounts.isEmpty()) {
            Discount bestDiscount = activeDiscounts.stream()
              .max((d1, d2) -> d1.getDiscountValue().compareTo(d2.getDiscountValue()))
              .orElse(null);
            return bestDiscount != null ? bestDiscount.getName() : null;
          }
        } catch (Exception e) {
          System.err.println("Error getting discount name: " + e.getMessage());
        }
        return null; 
      }
      
      @Override
      public OffsetDateTime getDiscountStartDate() { 
        try {
          List<Discount> activeDiscounts = discountService.findActiveDiscountsForProduct(product.getId());
          if (!activeDiscounts.isEmpty()) {
            Discount bestDiscount = activeDiscounts.stream()
              .max((d1, d2) -> d1.getDiscountValue().compareTo(d2.getDiscountValue()))
              .orElse(null);
            return bestDiscount != null ? bestDiscount.getStartDate() : null;
          }
        } catch (Exception e) {
          System.err.println("Error getting discount start date: " + e.getMessage());
        }
        return null; 
      }
      
      @Override
      public OffsetDateTime getDiscountEndDate() { 
        try {
          List<Discount> activeDiscounts = discountService.findActiveDiscountsForProduct(product.getId());
          if (!activeDiscounts.isEmpty()) {
            Discount bestDiscount = activeDiscounts.stream()
              .max((d1, d2) -> d1.getDiscountValue().compareTo(d2.getDiscountValue()))
              .orElse(null);
            return bestDiscount != null ? bestDiscount.getEndDate() : null;
          }
        } catch (Exception e) {
          System.err.println("Error getting discount end date: " + e.getMessage());
        }
        return null; 
      }
      
      @Override
      public BigDecimal getMinOrderValue() { 
        try {
          List<Discount> activeDiscounts = discountService.findActiveDiscountsForProduct(product.getId());
          if (!activeDiscounts.isEmpty()) {
            Discount bestDiscount = activeDiscounts.stream()
              .max((d1, d2) -> d1.getDiscountValue().compareTo(d2.getDiscountValue()))
              .orElse(null);
            return bestDiscount != null ? bestDiscount.getMinOrderValue() : BigDecimal.ZERO;
          }
        } catch (Exception e) {
          System.err.println("Error getting min order value: " + e.getMessage());
        }
        return BigDecimal.ZERO; 
      }
      
      @Override
      public String getShopName() { return product.getShop().getShopName(); }
      
      @Override
      public Integer getViewCount() { return product.getViewCount(); }
      
      @Override
      public Integer getSoldCount() { return product.getSoldCount(); }
      
      @Override
      public Double getRating() { return 0.0; }
      
      @Override
      public Integer getReviewCount() { return 0; }
    };
  }
}
