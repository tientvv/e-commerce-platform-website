package com.tientvv.service;

import com.tientvv.config.TimeZoneConfig;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;
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
import com.tientvv.repository.ReviewRepository;
import com.tientvv.specification.ProductSpecification;
import com.tientvv.model.Review;

@SuppressWarnings("unused")
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

  @Autowired
  private ReviewRepository reviewRepository;

  public List<Product> getAllProducts() {
    return productRepository.findAll();
  }

  public Product createProduct(CreateProductDto dto) throws Exception {
    Product product = new Product();
    
    // Validate và set category
    if (dto.getCategoryId() == null) {
      throw new RuntimeException("Category ID is required");
    }
    Category category = categoryRepository.findById(dto.getCategoryId())
        .orElseThrow(() -> new RuntimeException("Category not found"));
    product.setCategory(category);
    
    // Set basic fields
    product.setName(dto.getName() != null ? dto.getName().trim() : "");
    product.setBrand(dto.getBrand() != null ? dto.getBrand().trim() : "");
    product.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : "");
    
    // Handle image upload
    if (dto.getProductImage() != null && !dto.getProductImage().isEmpty()) {
      String imageUrl = imageUploadService.uploadImage(dto.getProductImage());
      product.setProductImage(imageUrl);
    } else {
      throw new RuntimeException("Product image is required");
    }
    
    // Set shop
    if (dto.getShopId() == null) {
      throw new RuntimeException("Shop ID is required");
    }
    Shop shop = shopRepository.findById(dto.getShopId())
        .orElseThrow(() -> new RuntimeException("Shop not found"));
    product.setShop(shop);
    
    // Set default values
    product.setIsActive(true);
    product.setViewCount(0);
    product.setSoldCount(0);
    
    return productRepository.save(product);
  }

  public Product updateProduct(UUID id, UpdateProductDto dto) throws Exception {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Product not found"));
    
    // Update basic fields - only update if provided
    if (dto.getName() != null) {
      product.setName(dto.getName().trim());
    }
    if (dto.getBrand() != null) {
      product.setBrand(dto.getBrand().trim());
    }
    if (dto.getDescription() != null) {
      product.setDescription(dto.getDescription().trim());
    }
    
    // Update category if provided
    if (dto.getCategoryId() != null) {
      Category category = categoryRepository.findById(dto.getCategoryId())
          .orElseThrow(() -> new RuntimeException("Category not found"));
      product.setCategory(category);
    }
    
    // Update image if provided
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
    OffsetDateTime currentTime = OffsetDateTime.now();
    return productRepository.findActiveProductsWithPricing(currentTime);
  }

  public List<ProductDisplayDto> findActiveProductsWithPricingByCategoryId(UUID categoryId) {
    OffsetDateTime currentTime = OffsetDateTime.now();
    return productRepository.findActiveProductsWithPricingByCategoryId(categoryId, currentTime);
  }

  public List<ProductDisplayDto> findActiveProductsWithActiveDiscounts() {
    // Lấy thời gian hiện tại theo múi giờ Việt Nam
    OffsetDateTime currentVietnamTime = TimeZoneConfig.getCurrentVietnamTime().toOffsetDateTime();
    return productRepository.findActiveProductsWithActiveDiscounts(currentVietnamTime);
  }

  public List<ProductDisplayDto> findActiveProductsWithProductSpecificDiscounts() {
    try {
      OffsetDateTime currentTime = OffsetDateTime.now();
      List<ProductDisplayDto> discountedProducts = productRepository.findActiveProductsWithProductSpecificDiscounts(currentTime);
      
      // Tạo danh sách mới với rating được tính từ reviews
      List<ProductDisplayDto> productsWithRating = new ArrayList<>();
      for (ProductDisplayDto product : discountedProducts) {
        try {
          // Fetch reviews cho sản phẩm này
          List<Review> reviews = reviewRepository.findByProductIdOrderByReviewDateDesc(product.getId());
          
          final double avgRating;
          final int reviewCount;
          
          if (reviews != null && !reviews.isEmpty()) {
            // Tính rating trung bình
            avgRating = reviews.stream()
              .mapToDouble(review -> review.getRating().doubleValue())
              .average()
              .orElse(0.0);
            
            // Đếm số reviews
            reviewCount = (int) reviews.stream()
              .filter(review -> review.getRating() != null)
              .count();
          } else {
            avgRating = 0.0;
            reviewCount = 0;
          }
          
          // Tạo ProductDisplayDto mới với rating đúng
          ProductDisplayDto productWithRating = new ProductDisplayDto() {
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
            public Integer getViewCount() { return product.getViewCount(); }
            @Override
            public Integer getSoldCount() { return product.getSoldCount(); }
            @Override
            public String getCategoryName() { return product.getCategoryName(); }
            @Override
            public UUID getCategoryId() { return product.getCategoryId(); }
            @Override
            public String getShopName() { return product.getShopName(); }
            @Override
            public UUID getShopId() { return product.getShopId(); }
            @Override
            public BigDecimal getMinPrice() { 
              return product.getMinPrice() != null ? product.getMinPrice() : BigDecimal.ZERO; 
            }
            @Override
            public BigDecimal getMaxPrice() { 
              return product.getMaxPrice() != null ? product.getMaxPrice() : BigDecimal.ZERO; 
            }
            @Override
            public BigDecimal getOriginalPrice() { 
              return product.getOriginalPrice() != null ? product.getOriginalPrice() : BigDecimal.ZERO; 
            }
            @Override
            public Double getRating() { return avgRating; }
            @Override
            public Integer getReviewCount() { return reviewCount; }
            @Override
            public Integer getDiscountPercentage() { 
              return product.getDiscountPercentage() != null ? product.getDiscountPercentage().intValue() : 0; 
            }
            @Override
            public Integer getDiscountAmount() { 
              return product.getDiscountAmount() != null ? product.getDiscountAmount().intValue() : 0; 
            }
            @Override
            public String getDiscountType() { return product.getDiscountType(); }
            @Override
            public String getDiscountName() { return product.getDiscountName(); }
            @Override
            public OffsetDateTime getDiscountStartDate() { return product.getDiscountStartDate(); }
            @Override
            public OffsetDateTime getDiscountEndDate() { return product.getDiscountEndDate(); }
            @Override
            public BigDecimal getMinOrderValue() { 
              return product.getMinOrderValue(); 
            }
          };
          
          productsWithRating.add(productWithRating);
        } catch (Exception e) {
          System.err.println("Error processing discounted product " + product.getId() + ": " + e.getMessage());
          // Nếu có lỗi, vẫn thêm sản phẩm gốc
          productsWithRating.add(product);
        }
      }
      
      return productsWithRating;
    } catch (Exception e) {
      System.err.println("Error in findActiveProductsWithProductSpecificDiscounts: " + e.getMessage());
      e.printStackTrace();
      // Fallback to original method
    OffsetDateTime currentTime = OffsetDateTime.now();
    return productRepository.findActiveProductsWithProductSpecificDiscounts(currentTime);
    }
  }

  public List<ProductDisplayDto> findProductsWithDiscountsSimple() {
    OffsetDateTime currentTime = OffsetDateTime.now();
    return productRepository.findActiveProductsWithProductSpecificDiscounts(currentTime);
  }

  public List<ProductDisplayDto> findProductsWithBestDiscounts() {
    // Lấy thời gian hiện tại theo múi giờ Việt Nam
    OffsetDateTime currentVietnamTime = TimeZoneConfig.getCurrentVietnamTime().toOffsetDateTime();
    
    // Lấy tất cả sản phẩm có discount
    List<ProductDisplayDto> productsWithDiscounts = productRepository.findActiveProductsWithActiveDiscounts(currentVietnamTime);
    
    // Loại bỏ trùng lặp sản phẩm và chỉ giữ lại sản phẩm với discount tốt nhất
    Map<UUID, ProductDisplayDto> uniqueProducts = new HashMap<>();
    
    for (ProductDisplayDto product : productsWithDiscounts) {
      UUID productId = product.getId();
      
      if (!uniqueProducts.containsKey(productId)) {
        // Sản phẩm chưa có trong map, thêm vào
        uniqueProducts.put(productId, product);
      } else {
        // Sản phẩm đã có, so sánh discount để giữ lại cái tốt hơn
        ProductDisplayDto existingProduct = uniqueProducts.get(productId);
        
        // Sử dụng method so sánh mới
        if (compareDiscounts(product, existingProduct) > 0) {
          uniqueProducts.put(productId, product);
        }
      }
    }
    
    // Chuyển về list và sắp xếp theo giá trị discount giảm dần
    List<ProductDisplayDto> result = uniqueProducts.values().stream()
        .sorted((p1, p2) -> compareDiscounts(p2, p1)) // Giảm dần
        .collect(Collectors.toList());
    
    return result;
  }
  
  // Helper method để tính giá trị discount
  private BigDecimal getDiscountValue(ProductDisplayDto product) {
    BigDecimal productPrice = product.getMinPrice() != null ? product.getMinPrice() : BigDecimal.ZERO;
    
    if (product.getDiscountPercentage() != null && product.getDiscountPercentage() > 0) {
      // Với discount percentage, tính giá trị thực tế
      BigDecimal percentageValue = new BigDecimal(product.getDiscountPercentage());
      BigDecimal result = productPrice.multiply(percentageValue).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
      return result;
    } else if (product.getDiscountAmount() != null && product.getDiscountAmount() > 0) {
      // Với discount fixed, trả về số tiền giảm trực tiếp
      BigDecimal result = new BigDecimal(product.getDiscountAmount());
      return result;
    }
    return BigDecimal.ZERO;
  }

  // Helper method để so sánh discount (ưu tiên percentage trước)
  private int compareDiscounts(ProductDisplayDto product1, ProductDisplayDto product2) {
    // Ưu tiên 1: Percentage discount luôn tốt hơn Fixed discount
    boolean hasPercentage1 = product1.getDiscountPercentage() != null && product1.getDiscountPercentage() > 0;
    boolean hasPercentage2 = product2.getDiscountPercentage() != null && product2.getDiscountPercentage() > 0;
    
    if (hasPercentage1 && !hasPercentage2) {
      return 1; // product1 tốt hơn
    }
    if (!hasPercentage1 && hasPercentage2) {
      return -1; // product2 tốt hơn
    }
    
    // Nếu cùng loại, so sánh giá trị thực tế
    BigDecimal value1 = getDiscountValue(product1);
    BigDecimal value2 = getDiscountValue(product2);
    
    return value1.compareTo(value2);
  }

  public List<ProductDisplayDto> getSuggestedProducts() {
      try {
          // Sử dụng query giống như CategoryView để đảm bảo logic discount đúng
          List<ProductDisplayDto> allProducts = new ArrayList<>();
          
          // Lấy tất cả category và sử dụng findActiveProductsWithPricingByCategoryId
          List<Category> categories = categoryRepository.findAll();
          for (Category category : categories) {
              List<ProductDisplayDto> categoryProducts = findActiveProductsWithPricingByCategoryId(category.getId());
              allProducts.addAll(categoryProducts);
          }
          
          // Tạo danh sách mới với rating được tính từ reviews
          List<ProductDisplayDto> productsWithRating = new ArrayList<>();
          for (ProductDisplayDto product : allProducts) {
              try {
                  // Fetch reviews cho sản phẩm này
                  List<Review> reviews = reviewRepository.findByProductIdOrderByReviewDateDesc(product.getId());
                  
                  final double avgRating;
                  final int reviewCount;
                  
                  if (reviews != null && !reviews.isEmpty()) {
                      // Tính rating trung bình
                      avgRating = reviews.stream()
                          .mapToDouble(review -> review.getRating().doubleValue())
                          .average()
                          .orElse(0.0);
                      
                      // Đếm số reviews
                      reviewCount = (int) reviews.stream()
                          .filter(review -> review.getRating() != null)
                          .count();
                  } else {
                      avgRating = 0.0;
                      reviewCount = 0;
                  }
                  
                  // Tạo ProductDisplayDto mới với rating đúng
                  ProductDisplayDto productWithRating = new ProductDisplayDto() {
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
                      public String getCategoryName() { return product.getCategoryName(); }
                      @Override
                      public UUID getCategoryId() { return product.getCategoryId(); }
                      @Override
                      public BigDecimal getMinPrice() { return product.getMinPrice(); }
                      @Override
                      public BigDecimal getMaxPrice() { return product.getMaxPrice(); }
                      @Override
                      public BigDecimal getOriginalPrice() { return product.getOriginalPrice(); }
                      @Override
                      public Integer getDiscountPercentage() { return product.getDiscountPercentage(); }
                      @Override
                      public Integer getDiscountAmount() { return product.getDiscountAmount(); }
                      @Override
                      public String getDiscountType() { return product.getDiscountType(); }
                      @Override
                      public String getDiscountName() { return product.getDiscountName(); }
                      @Override
                      public OffsetDateTime getDiscountStartDate() { return product.getDiscountStartDate(); }
                      @Override
                      public OffsetDateTime getDiscountEndDate() { return product.getDiscountEndDate(); }
                      @Override
                      public BigDecimal getMinOrderValue() { return product.getMinOrderValue(); }
                                  @Override
            public String getShopName() { return product.getShopName(); }
            @Override
            public UUID getShopId() { return product.getShopId(); }
            @Override
            public Integer getViewCount() { return product.getViewCount(); }
                      @Override
                      public Integer getSoldCount() { return product.getSoldCount(); }
                      @Override
                      public Double getRating() { return avgRating; }
                      @Override
                      public Integer getReviewCount() { return reviewCount; }
                  };
                  
                  productsWithRating.add(productWithRating);
              } catch (Exception e) {
                  System.err.println("Error calculating rating for product " + product.getId() + ": " + e.getMessage());
                  productsWithRating.add(product); // Thêm product gốc nếu có lỗi
              }
          }
          
          // Randomize danh sách
          Collections.shuffle(productsWithRating);
          
          // Trả về tối đa 20 sản phẩm
          return productsWithRating.stream().limit(20).collect(Collectors.toList());
      } catch (Exception e) {
          System.err.println("Error in getSuggestedProducts: " + e.getMessage());
          e.printStackTrace();
          // Trả về danh sách rỗng nếu có lỗi
          return new ArrayList<>();
      }
  }

  public ProductDto getProductDtoById(UUID id) {
    Product product = productRepository.findById(id).orElse(null);
    if (product == null) {
      return null;
    }
    return ProductDto.fromEntity(product);
  }

  public ProductDetailDto getProductDetailById(UUID id) {
    OffsetDateTime currentTime = OffsetDateTime.now();
    return productRepository.findProductDetailById(id, currentTime);
  }

  public ProductDto getProductForEdit(UUID id) {
    return productRepository.findProductForEdit(id);
  }

  public List<ProductDisplayDto> searchProducts(String query) {
    try {
      System.out.println("Searching for query: " + query);
      
      // Sử dụng search đơn giản thay vì Specification để tránh lỗi NCLOB
      List<Product> allProducts = productRepository.findAll();
      List<Product> products = allProducts.stream()
          .filter(p -> p.getIsActive() &&
              (p.getName() != null && p.getName().toLowerCase().contains(query.toLowerCase()) ||
                  p.getBrand() != null && p.getBrand().toLowerCase().contains(query.toLowerCase()) ||
                  p.getDescription() != null && p.getDescription().toLowerCase().contains(query.toLowerCase()) ||
                  p.getCategory() != null && p.getCategory().getName() != null && p.getCategory().getName().toLowerCase().contains(query.toLowerCase())))
          .collect(Collectors.toList());
      System.out.println("Raw products found: " + products.size());
      
      // Tạo danh sách mới với rating được tính từ reviews
      List<ProductDisplayDto> productsWithRating = new ArrayList<>();
      for (Product product : products) {
        try {
          // Fetch reviews cho sản phẩm này
          List<Review> reviews = reviewRepository.findByProductIdOrderByReviewDateDesc(product.getId());
          
          final double avgRating;
          final int reviewCount;
          
          if (reviews != null && !reviews.isEmpty()) {
            // Tính rating trung bình
            avgRating = reviews.stream()
              .mapToDouble(review -> review.getRating().doubleValue())
              .average()
              .orElse(0.0);
            
            // Đếm số reviews
            reviewCount = (int) reviews.stream()
              .filter(review -> review.getRating() != null)
              .count();
          } else {
            avgRating = 0.0;
            reviewCount = 0;
          }
          
          // Tạo ProductDisplayDto mới với rating đúng
          ProductDisplayDto productWithRating = new ProductDisplayDto() {
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
              return BigDecimal.ZERO;
            }
            @Override
            public BigDecimal getOriginalPrice() { 
              if (product.getProductVariants() != null && !product.getProductVariants().isEmpty()) {
                return product.getProductVariants().stream()
                  .filter(pv -> pv.getIsActive())
                  .map(pv -> pv.getPrice())
                  .min(BigDecimal::compareTo)
                  .orElse(BigDecimal.ZERO);
              }
              return BigDecimal.ZERO;
            }
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
            public UUID getShopId() { return product.getShop().getId(); }
            @Override
            public Integer getViewCount() { return product.getViewCount(); }
            @Override
            public Integer getSoldCount() { return product.getSoldCount(); }
            @Override
            public Double getRating() { return avgRating; }
            @Override
            public Integer getReviewCount() { return reviewCount; }
          };
          
          productsWithRating.add(productWithRating);
        } catch (Exception e) {
          System.err.println("Error calculating rating for product " + product.getId() + ": " + e.getMessage());
          // Thêm product với rating 0 nếu có lỗi
          ProductDisplayDto fallbackProduct = convertToProductDisplayDto(product);
          productsWithRating.add(fallbackProduct);
        }
      }
      
      System.out.println("Converted to DTO with rating: " + productsWithRating.size() + " products");
      return productsWithRating;
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
      public UUID getShopId() { return product.getShop().getId(); }
      
      @Override
      public Integer getViewCount() { return product.getViewCount(); }
      
      @Override
      public Integer getSoldCount() { return product.getSoldCount(); }
      
      @Override
      public Double getRating() { 
        try {
          if (product.getReviews() != null && !product.getReviews().isEmpty()) {
            double avgRating = product.getReviews().stream()
              .mapToDouble(review -> review.getRating().doubleValue())
              .average()
              .orElse(0.0);
            return avgRating;
          }
        } catch (Exception e) {
          System.err.println("Error calculating rating: " + e.getMessage());
        }
        return 0.0; 
      }
      
      @Override
      public Integer getReviewCount() { 
        try {
          if (product.getReviews() != null) {
            return (int) product.getReviews().stream()
              .filter(review -> review.getRating() != null)
              .count();
          }
        } catch (Exception e) {
          System.err.println("Error calculating review count: " + e.getMessage());
        }
        return 0; 
      }
    };
  }
}