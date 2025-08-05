package com.tientvv.config;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import javax.sql.DataSource;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.tientvv.model.Account;
import com.tientvv.model.Category;
import com.tientvv.model.Discount;
import com.tientvv.model.Payment;
import com.tientvv.model.Product;
import com.tientvv.model.ProductVariant;
import com.tientvv.model.Shop;
import com.tientvv.model.Shipping;
import com.tientvv.repository.AccountRepository;
import com.tientvv.repository.CategoryRepository;
import com.tientvv.repository.DiscountRepository;
import com.tientvv.repository.PaymentRepository;
import com.tientvv.repository.ProductRepository;
import com.tientvv.repository.ProductVariantRepository;
import com.tientvv.repository.ShopRepository;
import com.tientvv.repository.ShippingRepository;

@Component
public class DataInitializer implements CommandLineRunner {

  @Autowired
  private AccountRepository accountRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private ShippingRepository shippingRepository;

  @Autowired
  private DiscountRepository discountRepository;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private ProductVariantRepository productVariantRepository;

  @Autowired
  private ShopRepository shopRepository;

  @Autowired
  private PaymentRepository paymentRepository;

  @Autowired
  private DataSource dataSource;

  @Override
  public void run(String... args) throws Exception {
    checkDatabaseConnection();
    initializeAdminAccount();
  }

  private void checkDatabaseConnection() {
    try {
      dataSource.getConnection().close();
      System.out.println("✅ Database connection: OK");
    } catch (Exception e) {
      System.err.println("❌ Database connection failed: " + e.getMessage());
      throw new RuntimeException("Cannot connect to database", e);
    }
  }

  private void initializeAdminAccount() {
    try {
      // Check if admin account already exists
      if (accountRepository.findByUsernameAndIsActive("admin", true) != null) {
        System.out.println("✅ Admin account already exists, skipping initialization.");
        return;
      }

      // Create default admin account
      Account adminAccount = new Account();
      adminAccount.setUsername("admin");
      adminAccount.setName("Administrator");
      adminAccount.setEmail(null);
      adminAccount.setPassword(BCrypt.hashpw("admin@123", BCrypt.gensalt()));
      adminAccount.setPhone(null);
      adminAccount.setRole("ADMIN");
      adminAccount.setIsActive(true);
      adminAccount.setCreatedAt(OffsetDateTime.now());
      adminAccount.setUpdatedAt(OffsetDateTime.now());

      accountRepository.save(adminAccount);

      System.out.println("🔥 =================================");
      System.out.println("✅ Default Admin Account Created!");
      System.out.println("📧 Username: admin");
      System.out.println("🔑 Password: 123456");
      System.out.println("👤 Name: Administrator");
      System.out.println("📨 Email: admin@gmail.com");
      System.out.println("📱 Phone: 0123456789");
      System.out.println("🔒 Role: ADMIN");
      System.out.println("🔥 =================================");

    } catch (Exception e) {
      System.err.println("❌ Error creating admin account: " + e.getMessage());
    }
  }

  @SuppressWarnings("unused")
  private void initializeUserAccount() {
    try {
      // Check if demo user account already exists
      if (accountRepository.findByUsernameAndIsActive("demouser", true) != null) {
        System.out.println("✅ Demo user account already exists, skipping initialization.");
        return;
      }

      // Create demo user account
      Account userAccount = new Account();
      userAccount.setUsername("demouser");
      userAccount.setName("Demo User");
      userAccount.setEmail("user@example.com");
      userAccount.setPassword(BCrypt.hashpw("123456", BCrypt.gensalt()));
      userAccount.setPhone("0987654321");
      userAccount.setRole("USER");
      userAccount.setIsActive(true);
      userAccount.setCreatedAt(OffsetDateTime.now());
      userAccount.setUpdatedAt(OffsetDateTime.now());

      accountRepository.save(userAccount);

      System.out.println("👤 =================================");
      System.out.println("✅ Demo User Account Created!");
      System.out.println("📧 Username: demouser");
      System.out.println("🔑 Password: 123456");
      System.out.println("👤 Name: Demo User");
      System.out.println("📨 Email: user@example.com");
      System.out.println("📱 Phone: 0987654321");
      System.out.println("🔒 Role: USER");
      System.out.println("👤 =================================");

    } catch (Exception e) {
      System.err.println("❌ Error creating demo user account: " + e.getMessage());
    }
  }

  @SuppressWarnings("unused")
  private void initializeSampleCategories() {
    try {
      // Check if categories already exist
      if (categoryRepository.count() > 0) {
        System.out.println("✅ Categories already exist, skipping initialization.");
        return;
      }

      // Create sample categories
      String[] categoryNames = {
          "Điện thoại & Tablet",
          "Laptop & Máy tính",
          "Thời trang Nam",
          "Thời trang Nữ",
          "Giày dép",
          "Túi xách & Ví",
          "Đồng hồ",
          "Phụ kiện thời trang",
          "Gia dụng & Đời sống",
          "Sách & Văn phòng phẩm",
          "Thể thao & Du lịch",
          "Sức khỏe & Làm đẹp"
      };

      System.out.println("📦 Creating sample categories...");

      for (String categoryName : categoryNames) {
        if (!categoryRepository.existsByName(categoryName)) {
          Category category = new Category();
          category.setName(categoryName);
          category.setIsActive(true);
          // Note: categoryImage will be null for sample data

          categoryRepository.save(category);
          System.out.println("   ✅ Created category: " + categoryName);
        }
      }

      System.out.println("📦 =================================");
      System.out.println("✅ Sample Categories Created!");
      System.out.println("📊 Total categories: " + categoryNames.length);
      System.out.println("💡 Note: You can add images via Admin Panel");
      System.out.println("📦 =================================");

    } catch (Exception e) {
      System.err.println("❌ Error creating sample categories: " + e.getMessage());
    }
  }

  @SuppressWarnings("unused")
  private void initializeSampleShippings() {
    try {
      // Check if shippings already exist
      if (shippingRepository.count() > 0) {
        System.out.println("✅ Shippings already exist, skipping initialization.");
        return;
      }

      System.out.println("📦 Creating sample shippings...");

      // Create sample shipping 1
      Shipping shipping1 = new Shipping();
      shipping1.setShippingMethod("Giao hàng tiết kiệm");
      shipping1.setPrice(new BigDecimal("15000"));
      shipping1.setEstimatedDelivery("3-5 ngày làm việc");
      shipping1.setIsActive(true);
      shippingRepository.save(shipping1);
      System.out.println("   ✅ Created shipping: Giao hàng tiết kiệm");

      // Create sample shipping 2
      Shipping shipping2 = new Shipping();
      shipping2.setShippingMethod("Giao hàng nhanh");
      shipping2.setPrice(new BigDecimal("30000"));
      shipping2.setEstimatedDelivery("1-2 ngày làm việc");
      shipping2.setIsActive(true);
      shippingRepository.save(shipping2);
      System.out.println("   ✅ Created shipping: Giao hàng nhanh");

      // Create sample shipping 3
      Shipping shipping3 = new Shipping();
      shipping3.setShippingMethod("Giao hàng miễn phí");
      shipping3.setPrice(new BigDecimal("0"));
      shipping3.setEstimatedDelivery("3-7 ngày làm việc");
      shipping3.setIsActive(true);
      shippingRepository.save(shipping3);
      System.out.println("   ✅ Created shipping: Giao hàng miễn phí");

      System.out.println("📦 =================================");
      System.out.println("✅ Sample Shippings Created!");
      System.out.println("📦 Total shippings: 3");
      System.out.println("📦 =================================");

    } catch (Exception e) {
      System.err.println("❌ Error creating sample shippings: " + e.getMessage());
    }
  }

  @SuppressWarnings("unused")
  private void initializeSampleDiscounts() {
    try {
      // Check if discounts already exist
      if (discountRepository.count() > 0) {
        System.out.println("✅ Discounts already exist, skipping initialization.");
        return;
      }

      System.out.println("🎫 Creating sample discounts...");

      // Create sample discount 1 - Percentage
      Discount discount1 = new Discount();
      discount1.setName("SAVE10");
      discount1.setDescription("Giảm 10% cho đơn hàng từ 500k");
      discount1.setDiscountType("PERCENTAGE");
      discount1.setDiscountValue(new BigDecimal("10"));
      discount1.setStartDate(OffsetDateTime.now().minusDays(30));
      discount1.setEndDate(OffsetDateTime.now().plusDays(365));
      discount1.setMinOrderValue(new BigDecimal("500000"));
      discount1.setIsActive(true);
      discountRepository.save(discount1);
      System.out.println("   ✅ Created discount: SAVE10 (10% off)");

      // Create sample discount 2 - Fixed amount
      Discount discount2 = new Discount();
      discount2.setName("SAVE50K");
      discount2.setDescription("Giảm 50k cho đơn hàng từ 300k");
      discount2.setDiscountType("FIXED");
      discount2.setDiscountValue(new BigDecimal("50000"));
      discount2.setStartDate(OffsetDateTime.now().minusDays(30));
      discount2.setEndDate(OffsetDateTime.now().plusDays(365));
      discount2.setMinOrderValue(new BigDecimal("300000"));
      discount2.setIsActive(true);
      discountRepository.save(discount2);
      System.out.println("   ✅ Created discount: SAVE50K (50k off)");

      // Create sample discount 3 - Free shipping
      Discount discount3 = new Discount();
      discount3.setName("FREESHIP");
      discount3.setDescription("Miễn phí vận chuyển cho đơn hàng từ 1M");
      discount3.setDiscountType("FREESHIP");
      discount3.setDiscountValue(new BigDecimal("0"));
      discount3.setStartDate(OffsetDateTime.now().minusDays(30));
      discount3.setEndDate(OffsetDateTime.now().plusDays(365));
      discount3.setMinOrderValue(new BigDecimal("1000000"));
      discount3.setIsActive(true);
      discountRepository.save(discount3);
      System.out.println("   ✅ Created discount: FREESHIP (Free shipping)");

      System.out.println("🎫 =================================");
      System.out.println("✅ Sample Discounts Created!");
      System.out.println("🎫 Total discounts: 3");
      System.out.println("🎫 =================================");

    } catch (Exception e) {
      System.err.println("❌ Error creating sample discounts: " + e.getMessage());
    }
  }

  @SuppressWarnings("unused")
  private void initializeSampleProducts() {
    try {
      // Check if products already exist
      if (productRepository.count() > 0) {
        System.out.println("✅ Products already exist, skipping initialization.");
        return;
      }

      System.out.println("📦 Creating sample products...");

      // Get first category and shop for sample products
      Category firstCategory = categoryRepository.findAll().stream().findFirst().orElse(null);
      Account demoUser = accountRepository.findByUsernameAndIsActive("demouser", true);

      if (firstCategory == null || demoUser == null) {
        System.out.println("⚠️  Skipping product creation - need category and demo user first");
        return;
      }

      // Create sample shop
      if (shopRepository.count() == 0) {
        // Create demo shop
        Shop demoShop = new Shop();
        demoShop.setShopName("Demo Shop");
        demoShop.setDescription("Cửa hàng demo để test");
        demoShop.setAddress("123 Demo Street, Demo City");
        demoShop.setPhone("0123456789");
        demoShop.setEmail("demo@shop.com");
        demoShop.setShopImage("https://example.com/demo-logo.png");
        demoShop.setIsActive(true);
        demoShop.setCreatedAt(OffsetDateTime.now());
        demoShop.setLastUpdated(OffsetDateTime.now());

        // Create demo shop owner account
        Account demoShopOwner = new Account();
        demoShopOwner.setUsername("demoshop");
        demoShopOwner.setEmail("demoshop@example.com");
        demoShopOwner.setPassword("123456"); // In real app, this should be hashed
        demoShopOwner.setPhone("0123456789");
        demoShopOwner.setAddress("123 Demo Street, Demo City");
        demoShopOwner.setIsActive(true);
        demoShopOwner.setRole("SHOP_OWNER");
        demoShopOwner.setCreatedAt(OffsetDateTime.now());
        demoShopOwner.setUpdatedAt(OffsetDateTime.now());

        // Save account first
        demoShopOwner = accountRepository.save(demoShopOwner);

        // Set shop owner
        demoShop.setUser(demoShopOwner);

        // Save shop
        demoShop = shopRepository.save(demoShop);

        System.out.println("   ✅ Created demo shop: " + demoShop.getShopName());
        System.out.println("   ✅ Created demo shop owner: " + demoShopOwner.getUsername());
      }

      // Create sample product 1 - Mouse with variants
      Product mouseProduct = new Product();
      mouseProduct.setName("Chuột không dây Logitech G304 - Hàng chính hãng");
      mouseProduct.setDescription("Chuột gaming không dây với độ nhạy cao, pin bền");
      mouseProduct.setBrand("Logitech");
      mouseProduct.setCategory(firstCategory);
      mouseProduct.setShop(shopRepository.findAll().stream().findFirst().orElse(null)); // Use the created demo shop
      mouseProduct.setProductImage("https://example.com/mouse.jpg");
      mouseProduct.setIsActive(true);
      productRepository.save(mouseProduct);
      System.out.println("   ✅ Created product: " + mouseProduct.getName());

      // Create variants for mouse
      ProductVariant mouseVariant1 = new ProductVariant();
      mouseVariant1.setProduct(mouseProduct);
      mouseVariant1.setVariantName("Màu sắc");
      mouseVariant1.setVariantValue("Đen");
      mouseVariant1.setPrice(new BigDecimal("839000"));
      mouseVariant1.setQuantity(50);
      mouseVariant1.setIsActive(true);
      productVariantRepository.save(mouseVariant1);
      System.out.println("   ✅ Created variant: Đen - 839.000đ");

      ProductVariant mouseVariant2 = new ProductVariant();
      mouseVariant2.setProduct(mouseProduct);
      mouseVariant2.setVariantName("Màu sắc");
      mouseVariant2.setVariantValue("Trắng");
      mouseVariant2.setPrice(new BigDecimal("859000"));
      mouseVariant2.setQuantity(30);
      mouseVariant2.setIsActive(true);
      productVariantRepository.save(mouseVariant2);
      System.out.println("   ✅ Created variant: Trắng - 859.000đ");

      // Create sample product 2 - Keyboard with variants
      Product keyboardProduct = new Product();
      keyboardProduct.setName("Bàn phím cơ Logitech G Pro X - RGB");
      keyboardProduct.setDescription("Bàn phím gaming cơ học với đèn RGB");
      keyboardProduct.setBrand("Logitech");
      keyboardProduct.setCategory(firstCategory);
      keyboardProduct.setShop(shopRepository.findAll().stream().findFirst().orElse(null)); // Use the created demo shop
      keyboardProduct.setProductImage("https://example.com/keyboard.jpg");
      keyboardProduct.setIsActive(true);
      productRepository.save(keyboardProduct);
      System.out.println("   ✅ Created product: " + keyboardProduct.getName());

      // Create variants for keyboard
      ProductVariant keyboardVariant1 = new ProductVariant();
      keyboardVariant1.setProduct(keyboardProduct);
      keyboardVariant1.setVariantName("Switch");
      keyboardVariant1.setVariantValue("Blue");
      keyboardVariant1.setPrice(new BigDecimal("2500000"));
      keyboardVariant1.setQuantity(20);
      keyboardVariant1.setIsActive(true);
      productVariantRepository.save(keyboardVariant1);
      System.out.println("   ✅ Created variant: Blue Switch - 2.500.000đ");

      ProductVariant keyboardVariant2 = new ProductVariant();
      keyboardVariant2.setProduct(keyboardProduct);
      keyboardVariant2.setVariantName("Switch");
      keyboardVariant2.setVariantValue("Red");
      keyboardVariant2.setPrice(new BigDecimal("2500000"));
      keyboardVariant2.setQuantity(25);
      keyboardVariant2.setIsActive(true);
      productVariantRepository.save(keyboardVariant2);
      System.out.println("   ✅ Created variant: Red Switch - 2.500.000đ");

      System.out.println("📦 =================================");
      System.out.println("✅ Sample Products Created!");
      System.out.println("📦 Total products: 2");
      System.out.println("📦 Total variants: 4");
      System.out.println("📦 =================================");

    } catch (Exception e) {
      System.err.println("❌ Error creating sample products: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @SuppressWarnings("unused")
  private void initializeSamplePayments() {
    try {
      // Check if payments already exist
      if (paymentRepository.count() > 0) {
        System.out.println("✅ Payments already exist, skipping initialization.");
        return;
      }

      System.out.println("💳 Creating sample payments...");

      // Create sample payment 1 - Cash on delivery
      Payment payment1 = new Payment();
      payment1.setPaymentCode("COD");
      payment1.setPaymentType("CASH");
      payment1.setPaymentName("Thanh toán khi nhận hàng");
      payment1.setIcon("https://example.com/cod-icon.png");
      payment1.setDescription("Thanh toán bằng tiền mặt khi nhận hàng");
      payment1.setIsActive(true);
      paymentRepository.save(payment1);
      System.out.println("   ✅ Created payment: COD - Thanh toán khi nhận hàng");

             // Create sample payment 2 - PayOS (Bank payment)
        Payment payment2 = new Payment();
        payment2.setPaymentCode("PAYOS");
        payment2.setPaymentType("BANK");
        payment2.setPaymentName("Thanh toán qua ngân hàng");
        payment2.setIcon("https://example.com/payos-icon.png");
        payment2.setDescription("Thanh toán trực tuyến an toàn qua PayOS");
        payment2.setIsActive(true);
        paymentRepository.save(payment2);
        System.out.println("   ✅ Created payment: PAYOS - Thanh toán qua ngân hàng");

      // Create sample payment 3 - Credit card
      Payment payment3 = new Payment();
      payment3.setPaymentCode("CREDIT_CARD");
      payment3.setPaymentType("CARD");
      payment3.setPaymentName("Thẻ tín dụng");
      payment3.setIcon("https://example.com/credit-card-icon.png");
      payment3.setDescription("Thanh toán bằng thẻ tín dụng");
      payment3.setIsActive(true);
      paymentRepository.save(payment3);
      System.out.println("   ✅ Created payment: CREDIT_CARD - Thẻ tín dụng");

      // Create sample payment 4 - E-wallet
      Payment payment4 = new Payment();
      payment4.setPaymentCode("E_WALLET");
      payment4.setPaymentType("WALLET");
      payment4.setPaymentName("Ví điện tử");
      payment4.setIcon("https://example.com/e-wallet-icon.png");
      payment4.setDescription("Thanh toán qua ví điện tử");
      payment4.setIsActive(true);
      paymentRepository.save(payment4);
      System.out.println("   ✅ Created payment: E_WALLET - Ví điện tử");

      System.out.println("✅ Sample payments created successfully!");
    } catch (Exception e) {
      System.err.println("❌ Error creating sample payments: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @SuppressWarnings("unused")
  private void showInitializationSummary() {
    System.out.println("📊 =================================");
    System.out.println("📊 INITIALIZATION SUMMARY");
    System.out.println("📊 =================================");

    try {
      long totalAccounts = accountRepository.count();
      long totalCategories = categoryRepository.count();
      long adminAccounts = accountRepository.findAll().stream()
          .filter(account -> "ADMIN".equals(account.getRole()))
          .count();

      System.out.println("👥 Total Accounts: " + totalAccounts);
      System.out.println("🔥 Admin Accounts: " + adminAccounts);
      System.out.println("📦 Total Categories: " + totalCategories);
      System.out.println("🌐 Frontend URL: http://localhost:5173");
      System.out.println("🔧 Admin Panel: http://localhost:5173/admin");
      System.out.println("📚 Documentation: See SETUP.md");

    } catch (Exception e) {
      System.err.println("❌ Error generating summary: " + e.getMessage());
    }

    System.out.println("📊 =================================");
  }
}