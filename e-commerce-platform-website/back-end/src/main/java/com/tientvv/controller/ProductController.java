package com.tientvv.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.tientvv.dto.CrudProduct.CreateProductDto;
import com.tientvv.dto.CrudProduct.ProductDto;
import com.tientvv.dto.CrudProduct.ProductDetailDto;
import com.tientvv.dto.CrudProduct.ProductDisplayDto;
import com.tientvv.dto.CrudProduct.UpdateProductDto;
import com.tientvv.model.Account;
import com.tientvv.model.Product;
import com.tientvv.service.ProductService;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.tientvv.service.ImageUploadService;
import com.tientvv.model.Discount;
import com.tientvv.repository.DiscountRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import com.tientvv.model.Category;
import com.tientvv.repository.CategoryRepository;
import com.tientvv.model.Shop;
import com.tientvv.repository.ShopRepository;
import com.tientvv.model.ProductVariant;
import com.tientvv.repository.ProductVariantRepository;
import com.tientvv.repository.AccountRepository;
import com.tientvv.repository.ProductRepository;
@RestController
@RequestMapping("/api/products")
public class ProductController {

  @Autowired
  private ProductService productService;

  @Autowired
  private ImageUploadService imageUploadService;

  @Autowired
  private com.tientvv.service.ShopService shopService;

  @Autowired
  private DiscountRepository discountRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private ShopRepository shopRepository;

  @Autowired
  private ProductVariantRepository productVariantRepository;

  @Autowired
  private AccountRepository accountRepository;

  @Autowired
  private ProductRepository productRepository;

  @GetMapping("/user")
  public Map<String, Object> getUserProducts(
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String categoryId,
      @RequestParam(required = false) String status,
      HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");

    if (account == null) {
      response.put("message", "Bạn chưa đăng nhập!");
      return response;
    }

    try {
      // Lấy thông tin shop của user
      com.tientvv.dto.shop.ShopDto shopDto = shopService.getShopByUserId(account.getId());
      if (shopDto == null) {
        response.put("message", "Bạn chưa đăng ký cửa hàng!");
        response.put("products", List.of()); // Trả về danh sách rỗng
        return response;
      }

      // Lấy tất cả sản phẩm của shop (bao gồm cả active và inactive)
      List<Product> products = productService.findAllByShopId(shopDto.getId());

      // Lọc theo tìm kiếm
      if (search != null && !search.trim().isEmpty()) {
        String searchTerm = search.trim().toLowerCase();
        products = products.stream()
            .filter(product -> 
                product.getName().toLowerCase().contains(searchTerm) ||
                (product.getBrand() != null && product.getBrand().toLowerCase().contains(searchTerm)) ||
                (product.getDescription() != null && product.getDescription().toLowerCase().contains(searchTerm))
            )
            .collect(Collectors.toList());
      }

      // Lọc theo danh mục
      if (categoryId != null && !categoryId.isEmpty()) {
        UUID categoryUuid = UUID.fromString(categoryId);
        products = products.stream()
            .filter(product -> product.getCategory().getId().equals(categoryUuid))
            .collect(Collectors.toList());
      }

      // Lọc theo trạng thái
      if (status != null && !status.isEmpty()) {
        boolean isActive = "active".equals(status);
        products = products.stream()
            .filter(product -> product.getIsActive() == isActive)
            .collect(Collectors.toList());
      }

      // Convert to DTOs
      List<ProductDto> productDtos = products.stream()
          .map(ProductDto::fromEntity)
          .collect(Collectors.toList());

      response.put("products", productDtos);
      response.put("totalCount", productDtos.size());
      if (search != null && !search.trim().isEmpty()) {
        response.put("searchTerm", search.trim());
      }
    } catch (Exception e) {
      response.put("message", "Lỗi khi tải danh sách sản phẩm: " + e.getMessage());
      response.put("products", List.of());
    }

    return response;
  }

  @GetMapping
  public Map<String, Object> getProducts() {
    Map<String, Object> response = new HashMap<>();
    try {
      List<ProductDisplayDto> products = productService.findActiveProductsWithPricing();

      response.put("products", products);
    } catch (Exception e) {
      response.put("message", e.getMessage());
    }
    return response;
  }

  @GetMapping("/all")
  public Map<String, Object> getAllProducts(
      @RequestParam(required = false) String shopId,
      @RequestParam(required = false) String categoryId) {
    Map<String, Object> response = new HashMap<>();
    try {
      if (categoryId != null && !categoryId.isEmpty()) {
        UUID categoryUuid = UUID.fromString(categoryId);
        List<ProductDisplayDto> products = productService.findActiveProductsWithPricingByCategoryId(categoryUuid);
        response.put("products", products);
      } else if (shopId != null && !"your-shop-id".equals(shopId)) {
        UUID uuid = UUID.fromString(shopId);
        List<Product> products = productService.findAllByShopIdAndIsActiveTrue(uuid);
        List<ProductDto> productDtos = products.stream()
            .map(ProductDto::fromEntity)
            .collect(Collectors.toList());
        response.put("products", productDtos);
      } else {
        List<ProductDisplayDto> products = productService.findActiveProductsWithPricing();
        response.put("products", products);
      }
    } catch (Exception e) {
      response.put("message", e.getMessage());
    }
    return response;
  }

  @GetMapping("/index")
  public Map<String, Object> getAllProductsByShop(
      @RequestParam UUID shopId,
      @RequestParam Boolean isActive,
      HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");
    if (account == null) {
      response.put("message", "Bạn chưa đăng nhập!");
      return response;
    }

    List<ProductDto> products = productService.getProductByShopIdAndIsActive(shopId, isActive);
    response.put("products", products);
    return response;
  }

  @PostMapping("/add")
  public Map<String, Object> createProduct(@ModelAttribute CreateProductDto dto, HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");
    if (account == null) {
      response.put("message", "Bạn chưa đăng nhập!");
      return response;
    }

    // Validation
    if (dto.getName() == null || dto.getName().trim().isEmpty()) {
      response.put("errorMessage", "Vui lòng nhập tên sản phẩm!");
      return response;
    }
    if (dto.getBrand() == null || dto.getBrand().trim().isEmpty()) {
      response.put("errorMessage", "Vui lòng nhập thương hiệu sản phẩm!");
      return response;
    }
    if (dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
      response.put("errorMessage", "Vui lòng nhập mô tả sản phẩm!");
      return response;
    }
    if (dto.getCategoryId() == null) {
      response.put("errorMessage", "Vui lòng chọn danh mục cho sản phẩm!");
      return response;
    }
    if (dto.getProductImage() == null || dto.getProductImage().isEmpty()) {
      response.put("errorMessage", "Vui lòng chọn hình ảnh sản phẩm!");
      return response;
    }

    // Lấy shopId từ user session
    try {
      com.tientvv.dto.shop.ShopDto shopDto = shopService.getShopByUserId(account.getId());
      if (shopDto == null) {
        response.put("errorMessage", "Bạn chưa đăng ký cửa hàng!");
        return response;
      }
      dto.setShopId(shopDto.getId());
    } catch (Exception e) {
      response.put("errorMessage", "Lỗi khi lấy thông tin cửa hàng: " + e.getMessage());
      return response;
    }

    try {
      productService.createProduct(dto);
      response.put("message", "Sản phẩm đã được tạo thành công!");
    } catch (Exception e) {
      response.put("errorMessage", "Lỗi khi tạo sản phẩm: " + e.getMessage());
    }
    return response;
  }

  @PostMapping("/upload-image")
  public Map<String, Object> uploadImage(@RequestParam("file") MultipartFile file, HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");
    if (account == null) {
      response.put("message", "Bạn chưa đăng nhập!");
      return response;
    }

    try {
      if (file == null || file.isEmpty()) {
        response.put("message", "Vui lòng chọn file ảnh!");
        return response;
      }

      String imageUrl = imageUploadService.uploadImage(file);
      response.put("message", "Upload ảnh thành công!");
      response.put("url", imageUrl);
    } catch (Exception e) {
      response.put("message", "Lỗi khi upload ảnh: " + e.getMessage());
    }
    return response;
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getProductById(@PathVariable UUID id) {
    ProductDetailDto productDetail = productService.getProductDetailById(id);
    if (productDetail == null) {
      Map<String, String> response = new HashMap<>();
      response.put("message", "Không tìm thấy sản phẩm!");
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    return ResponseEntity.ok(productDetail);
  }

  @GetMapping("/edit/{id}")
  public ResponseEntity<?> getProductForEdit(@PathVariable UUID id, HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");
    if (account == null) {
      response.put("message", "Bạn chưa đăng nhập!");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    try {
      ProductDto product = productService.getProductForEdit(id);
      if (product == null) {
        response.put("message", "Không tìm thấy sản phẩm!");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
      }
      return ResponseEntity.ok(product);
    } catch (Exception e) {
      response.put("message", "Lỗi khi tải thông tin sản phẩm: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @PutMapping("/update/{id}")
  public Map<String, Object> updateProduct(@PathVariable UUID id, @ModelAttribute UpdateProductDto dto) {
    Map<String, Object> response = new HashMap<>();

    if (dto.getName() == null || dto.getName().trim().isEmpty()) {
      response.put("errorMessage", "Vui lòng nhập tên sản phẩm!");
      return response;
    }

    if (dto.getBrand() == null || dto.getBrand().trim().isEmpty()) {
      response.put("errorMessage", "Vui lòng nhập thương hiệu sản phẩm!");
      return response;
    }

    if (dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
      response.put("errorMessage", "Vui lòng nhập mô tả sản phẩm!");
      return response;
    }
    // CategoryId is optional in update operation

    try {
      productService.updateProduct(id, dto);
      response.put("message", "Sản phẩm đã được cập nhật thành công!");
    } catch (Exception e) {
      response.put("errorMessage", "Lỗi khi cập nhật sản phẩm: " + e.getMessage());
    }
    return response;
  }

  @PutMapping("/delete/{id}")
  public Map<String, Object> deleteProduct(@PathVariable UUID id, HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");
    if (account == null) {
      response.put("message", "Bạn chưa đăng nhập!");
      return response;
    }
    if (id == null) {
      response.put("message", "ID sản phẩm không hợp lệ!");
      return response;
    }
    productService.deleteProduct(id);
    response.put("message", "Đã ẩn sản phẩm thành công!");
    return response;
  }

  @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
  public Map<String, Object> deleteProductREST(@PathVariable UUID id, HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");
    if (account == null) {
      response.put("message", "Bạn chưa đăng nhập!");
      return response;
    }
    if (id == null) {
      response.put("message", "ID sản phẩm không hợp lệ!");
      return response;
    }
    productService.deleteProduct(id);
    response.put("message", "Đã ẩn sản phẩm thành công!");
    return response;
  }

  @PutMapping("/restore/{id}")
  public Map<String, Object> restoreProduct(@PathVariable UUID id, HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");
    if (account == null) {
      response.put("message", "Bạn chưa đăng nhập!");
      return response;
    }
    if (id == null) {
      response.put("message", "ID sản phẩm không hợp lệ!");
      return response;
    }
    productService.restoreProduct(id);
    response.put("message", "Đã khôi phục sản phẩm thành công!");
    return response;
  }

  @GetMapping("/discounted")
  public Map<String, Object> getDiscountedProducts() {
    Map<String, Object> response = new HashMap<>();
    try {
      // Sử dụng query đơn giản và để frontend xử lý logic so sánh
      List<ProductDisplayDto> discountedProducts = productService.findActiveProductsWithProductSpecificDiscounts();

      response.put("products", discountedProducts);
      response.put("message", "Lấy danh sách sản phẩm giảm giá thành công");
      response.put("count", discountedProducts.size());
    } catch (Exception e) {
      response.put("message", "Lỗi: " + e.getMessage());
      response.put("products", List.of());
      response.put("count", 0);
    }
    return response;
  }

  @GetMapping("/discounted/simple")
  public Map<String, Object> getDiscountedProductsSimple() {
    Map<String, Object> response = new HashMap<>();
    try {
      // Sử dụng method có sẵn
      List<ProductDisplayDto> discountedProducts = productService.findActiveProductsWithProductSpecificDiscounts();

      response.put("products", discountedProducts);
      response.put("message", "Lấy danh sách sản phẩm có discount cụ thể thành công");
      response.put("count", discountedProducts.size());
    } catch (Exception e) {
      response.put("message", "Lỗi: " + e.getMessage());
      response.put("products", List.of());
      response.put("count", 0);
    }
    return response;
  }

  @GetMapping("/discounted/very-simple")
  public Map<String, Object> getDiscountedProductsVerySimple() {
    Map<String, Object> response = new HashMap<>();
    try {
      // Sử dụng query rất đơn giản
      List<ProductDisplayDto> discountedProducts = productService.findProductsWithDiscountsSimple();

      response.put("products", discountedProducts);
      response.put("message", "Lấy danh sách sản phẩm có discount (query đơn giản) thành công");
      response.put("count", discountedProducts.size());
    } catch (Exception e) {
      response.put("message", "Lỗi: " + e.getMessage());
      response.put("products", List.of());
      response.put("count", 0);
    }
    return response;
  }

  @GetMapping("/discounted/debug")
  public Map<String, Object> getDiscountedProductsDebug() {
    Map<String, Object> response = new HashMap<>();
    try {
      // Lấy tất cả discount đang hoạt động
      List<Discount> activeDiscounts = discountRepository.findByIsActiveTrueOrderByStartDateDesc();

      // Lấy tất cả sản phẩm
      List<Product> allProducts = productService.findAllActiveProducts();

      // Lấy sản phẩm có discount
      List<ProductDisplayDto> discountedProducts = productService.findActiveProductsWithActiveDiscounts();

      response.put("activeDiscounts", activeDiscounts.stream().map(d -> {
        Map<String, Object> discountMap = new HashMap<>();
        discountMap.put("id", d.getId());
        discountMap.put("name", d.getName());
        discountMap.put("type", d.getDiscountType());
        discountMap.put("value", d.getDiscountValue());
        discountMap.put("startDate", d.getStartDate());
        discountMap.put("endDate", d.getEndDate());
        discountMap.put("productId", d.getProduct() != null ? d.getProduct().getId() : null);
        discountMap.put("categoryId", d.getCategory() != null ? d.getCategory().getId() : null);
        discountMap.put("isActive", d.getIsActive());
        return discountMap;
      }).collect(Collectors.toList()));

      response.put("totalProducts", allProducts.size());
      response.put("discountedProducts", discountedProducts);
      response.put("discountedCount", discountedProducts.size());
      response.put("message", "Debug info retrieved successfully");
    } catch (Exception e) {
      response.put("message", "Lỗi: " + e.getMessage());
      response.put("error", e.toString());
    }
    return response;
  }

  @GetMapping("/discounts/active")
  public Map<String, Object> getActiveDiscounts() {
    Map<String, Object> response = new HashMap<>();
    try {
      List<Discount> activeDiscounts = discountRepository.findByIsActiveTrueOrderByStartDateDesc();

      List<Map<String, Object>> discountInfo = activeDiscounts.stream()
          .map(d -> {
            Map<String, Object> discountMap = new HashMap<>();
            discountMap.put("id", d.getId());
            discountMap.put("name", d.getName());
            discountMap.put("description", d.getDescription());
            discountMap.put("type", d.getDiscountType());
            discountMap.put("value", d.getDiscountValue());
            discountMap.put("startDate", d.getStartDate());
            discountMap.put("endDate", d.getEndDate());
            discountMap.put("minOrderValue", d.getMinOrderValue());
            discountMap.put("applicationType", getApplicationType(d));
            discountMap.put("productName", d.getProduct() != null ? d.getProduct().getName() : null);
            discountMap.put("categoryName", d.getCategory() != null ? d.getCategory().getName() : null);
            return discountMap;
          })
          .collect(Collectors.toList());

      response.put("discounts", discountInfo);
      response.put("message", "Lấy danh sách discount đang hoạt động thành công");
      response.put("count", activeDiscounts.size());
    } catch (Exception e) {
      response.put("message", "Lỗi: " + e.getMessage());
      response.put("discounts", List.of());
      response.put("count", 0);
    }
    return response;
  }

  private String getApplicationType(Discount discount) {
    if (discount.getProduct() != null) {
      return "PRODUCT";
    } else if (discount.getCategory() != null) {
      return "CATEGORY";
    } else if (discount.getProductVariant() != null) {
      return "VARIANT";
    } else {
      return "GLOBAL";
    }
  }

  @PostMapping("/create-sample-data")
  public Map<String, Object> createSampleData() {
    Map<String, Object> response = new HashMap<>();
    try {
      // Tạo sản phẩm mẫu nếu chưa có
      List<Product> existingProducts = productService.findAllActiveProducts();
      if (existingProducts.isEmpty()) {
        response.put("message", "Không có sản phẩm nào. Vui lòng tạo sản phẩm trước.");
        return response;
      }

      // Tạo discount cho sản phẩm đầu tiên
      Product firstProduct = existingProducts.get(0);

      Discount productDiscount = new Discount();
      productDiscount.setName("SALE20");
      productDiscount.setDescription("Giảm 20% cho " + firstProduct.getName());
      productDiscount.setDiscountType("PERCENTAGE");
      productDiscount.setDiscountValue(new BigDecimal("20"));
      productDiscount.setStartDate(OffsetDateTime.now().minusDays(30));
      productDiscount.setEndDate(OffsetDateTime.now().plusDays(365));
      productDiscount.setMinOrderValue(new BigDecimal("0"));
      productDiscount.setProduct(firstProduct);
      productDiscount.setIsActive(true);
      discountRepository.save(productDiscount);

      // Tạo discount cho sản phẩm thứ hai nếu có
      if (existingProducts.size() > 1) {
        Product secondProduct = existingProducts.get(1);

        Discount productDiscount2 = new Discount();
        productDiscount2.setName("SALE50K");
        productDiscount2.setDescription("Giảm 50k cho " + secondProduct.getName());
        productDiscount2.setDiscountType("FIXED");
        productDiscount2.setDiscountValue(new BigDecimal("50000"));
        productDiscount2.setStartDate(OffsetDateTime.now().minusDays(30));
        productDiscount2.setEndDate(OffsetDateTime.now().plusDays(365));
        productDiscount2.setMinOrderValue(new BigDecimal("0"));
        productDiscount2.setProduct(secondProduct);
        productDiscount2.setIsActive(true);
        discountRepository.save(productDiscount2);
      }

      // Tạo global discount
      Discount globalDiscount = new Discount();
      globalDiscount.setName("GLOBAL10");
      globalDiscount.setDescription("Giảm 10% toàn bộ sản phẩm");
      globalDiscount.setDiscountType("PERCENTAGE");
      globalDiscount.setDiscountValue(new BigDecimal("10"));
      globalDiscount.setStartDate(OffsetDateTime.now().minusDays(30));
      globalDiscount.setEndDate(OffsetDateTime.now().plusDays(365));
      globalDiscount.setMinOrderValue(new BigDecimal("100000"));
      globalDiscount.setIsActive(true);
      discountRepository.save(globalDiscount);

      response.put("message", "Đã tạo dữ liệu mẫu thành công!");
      response.put("createdDiscounts", 3);

    } catch (Exception e) {
      response.put("message", "Lỗi: " + e.getMessage());
    }
    return response;
  }

  @PostMapping("/create-sample-products")
  public Map<String, Object> createSampleProducts() {
    Map<String, Object> response = new HashMap<>();
    try {
      // Kiểm tra xem đã có sản phẩm chưa
      if (productService.findAllActiveProducts().size() > 0) {
        response.put("message", "Đã có sản phẩm trong hệ thống.");
        return response;
      }

      // Lấy category đầu tiên
      List<Category> categories = categoryRepository.findAll();
      if (categories.isEmpty()) {
        response.put("message", "Không có category nào. Vui lòng tạo category trước.");
        return response;
      }

      // Lấy shop đầu tiên hoặc tạo shop mới
      List<Shop> shops = shopRepository.findAll();
      Shop shop;
      if (shops.isEmpty()) {
        // Tạo shop mới
        Account shopOwner = accountRepository.findByUsernameAndIsActive("admin", true);
        if (shopOwner == null) {
          response.put("message", "Không tìm thấy tài khoản admin.");
          return response;
        }

        shop = new Shop();
        shop.setShopName("Demo Shop");
        shop.setDescription("Cửa hàng demo");
        shop.setAddress("123 Demo Street");
        shop.setPhone("0123456789");
        shop.setEmail("demo@shop.com");
        shop.setIsActive(true);
        shop.setUser(shopOwner);
        shop.setCreatedAt(OffsetDateTime.now());
        shop.setLastUpdated(OffsetDateTime.now());
        shop = shopRepository.save(shop);
      } else {
        shop = shops.get(0);
      }

      // Tạo sản phẩm 1
      Product product1 = new Product();
      product1.setName("iPhone 15 Pro Max - 256GB");
      product1.setDescription("iPhone 15 Pro Max với chip A17 Pro, camera 48MP");
      product1.setBrand("Apple");
      product1.setCategory(categories.get(0));
      product1.setShop(shop);
      product1.setProductImage("https://example.com/iphone15.jpg");
      product1.setIsActive(true);
      product1 = productRepository.save(product1);

      // Tạo variant cho sản phẩm 1
      ProductVariant variant1 = new ProductVariant();
      variant1.setProduct(product1);
      variant1.setVariantName("Màu sắc");
      variant1.setVariantValue("Titan tự nhiên");
      variant1.setPrice(new BigDecimal("29990000"));
      variant1.setQuantity(10);
      variant1.setIsActive(true);
      productVariantRepository.save(variant1);

      // Tạo sản phẩm 2
      Product product2 = new Product();
      product2.setName("MacBook Air M2 - 13 inch");
      product2.setDescription("MacBook Air với chip M2, màn hình Retina");
      product2.setBrand("Apple");
      product2.setCategory(categories.get(0));
      product2.setShop(shop);
      product2.setProductImage("https://example.com/macbook-air.jpg");
      product2.setIsActive(true);
      product2 = productRepository.save(product2);

      // Tạo variant cho sản phẩm 2
      ProductVariant variant2 = new ProductVariant();
      variant2.setProduct(product2);
      variant2.setVariantName("Cấu hình");
      variant2.setVariantValue("8GB RAM, 256GB SSD");
      variant2.setPrice(new BigDecimal("25990000"));
      variant2.setQuantity(5);
      variant2.setIsActive(true);
      productVariantRepository.save(variant2);

      response.put("message", "Đã tạo sản phẩm mẫu thành công!");
      response.put("createdProducts", 2);

    } catch (Exception e) {
      response.put("message", "Lỗi: " + e.getMessage());
    }
    return response;
  }

  @GetMapping("/suggestions")
  public ResponseEntity<Map<String, Object>> getSuggestedProducts() {
    try {
      List<ProductDisplayDto> suggestedProducts = productService.getSuggestedProducts();
      Map<String, Object> response = new HashMap<>();
      response.put("products", suggestedProducts);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> response = new HashMap<>();
      response.put("error", "Không thể tải gợi ý sản phẩm");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @GetMapping("/search")
  public ResponseEntity<Map<String, Object>> searchProducts(@RequestParam String query) {
    try {
      System.out.println("=== SEARCH DEBUG ===");
      System.out.println("Query received: '" + query + "'");

      // Sử dụng method searchProducts đã được cập nhật trong ProductService
      List<ProductDisplayDto> resultDtos = productService.searchProducts(query);

      System.out.println("Final DTO results: " + resultDtos.size());
      System.out.println("=== END SEARCH DEBUG ===");

      Map<String, Object> response = new HashMap<>();
      response.put("products", resultDtos);
      response.put("query", query);
      response.put("count", resultDtos.size());
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      System.err.println("Search error: " + e.getMessage());
      e.printStackTrace();
      Map<String, Object> response = new HashMap<>();
      response.put("error", "Không thể tìm kiếm sản phẩm: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @GetMapping("/debug/products")
  public ResponseEntity<Map<String, Object>> debugProducts() {
    try {
      List<Product> allProducts = productRepository.findAll();
      List<Map<String, Object>> productInfo = new ArrayList<>();

      for (Product product : allProducts) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", product.getId());
        info.put("name", product.getName());
        info.put("brand", product.getBrand());
        info.put("isActive", product.getIsActive());
        info.put("category", product.getCategory() != null ? product.getCategory().getName() : "null");
        info.put("shop", product.getShop() != null ? product.getShop().getShopName() : "null");
        info.put("variantsCount", product.getProductVariants() != null ? product.getProductVariants().size() : 0);
        productInfo.add(info);
      }

      Map<String, Object> response = new HashMap<>();
      response.put("totalProducts", allProducts.size());
      response.put("activeProducts", allProducts.stream().filter(p -> p.getIsActive()).count());
      response.put("products", productInfo);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> response = new HashMap<>();
      response.put("error", "Không thể debug products: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }
}