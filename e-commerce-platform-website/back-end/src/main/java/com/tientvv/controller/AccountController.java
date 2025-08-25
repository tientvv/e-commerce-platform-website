package com.tientvv.controller;

import java.util.*;
import org.springframework.web.bind.annotation.*;
import com.tientvv.dto.account.*;
import com.tientvv.model.Account;
import com.tientvv.repository.AccountRepository;
import com.tientvv.service.AccountService;
import com.tientvv.service.ImageUploadService;
import com.tientvv.service.GoogleAuthService;
import com.tientvv.service.PasswordStrengthService;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class AccountController {

  @Autowired
  private AccountService accountService;

  @Autowired
  private AccountRepository accountRepository;

  @Autowired
  private ImageUploadService imageUploadService;

  @Autowired
  private GoogleAuthService googleAuthService;

  @Autowired
  private PasswordStrengthService passwordStrengthService;

  @PostMapping("/logout")
  public Map<String, Object> logout(HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");
    if (account == null) {
      response.put("message", "Bạn chưa đăng nhập!");
      return response;
    }
    session.removeAttribute("account");
    response.put("message", "Đăng xuất thành công!");
    return response;
  }

  @GetMapping("/info-account")
  public Map<String, Object> getInfoAccount(HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");
    if (account == null) {
      response.put("message", "Bạn chưa đăng nhập!");
      return response;
    }
    
    // Log để debug encoding
    System.out.println("Account name in session: '" + account.getName() + "'");
    System.out.println("Account name bytes: " + java.util.Arrays.toString(account.getName() != null ? account.getName().getBytes(java.nio.charset.StandardCharsets.UTF_8) : "null".getBytes()));
    
    // Trả về thông tin đầy đủ hơn
    Map<String, Object> accountInfo = new HashMap<>();
    accountInfo.put("id", account.getId().toString());
    accountInfo.put("username", account.getUsername());
    accountInfo.put("name", account.getName());
    accountInfo.put("email", account.getEmail());
    accountInfo.put("phone", account.getPhone());
    accountInfo.put("address", account.getAddress());
    accountInfo.put("role", account.getRole());
    accountInfo.put("accountsImage", account.getAccountsImage());
    accountInfo.put("googleId", account.getGoogleId());
    accountInfo.put("createdAt", account.getCreatedAt());
    accountInfo.put("updatedAt", account.getUpdatedAt());
    accountInfo.put("lastLogin", account.getLastLogin());
    accountInfo.put("isActive", account.getIsActive());
    
    // Thêm shopId nếu user có shop
    if (account.getShop() != null) {
      accountInfo.put("shopId", account.getShop().getId().toString());
    }
    
    response.put("account", accountInfo);
    return response;
  }

  @GetMapping("/test-encoding")
  public Map<String, Object> testEncoding() {
    Map<String, Object> response = new HashMap<>();
    
    // Test với tên tiếng Việt
    String testName = "Thủy Huỳnh Thị Thanh";
    String encodedName = "Thá»?y Huá»?nh Thá»? Thanh"; // Tên bị lỗi encoding
    
    System.out.println("Original test name: '" + testName + "'");
    System.out.println("Encoded test name: '" + encodedName + "'");
    
    // Test fix encoding
    String fixedName = com.tientvv.utils.EncodingUtils.fixVietnameseEncoding(encodedName);
    System.out.println("Fixed test name: '" + fixedName + "'");
    
    response.put("originalName", testName);
    response.put("encodedName", encodedName);
    response.put("fixedName", fixedName);
    response.put("message", "Test encoding thành công");
    
    return response;
  }

  @PostMapping("/test-multipart")
  public Map<String, Object> testMultipart(@RequestParam("file") MultipartFile file) {
    Map<String, Object> response = new HashMap<>();
    
    try {
      System.out.println("Test multipart - File name: " + file.getOriginalFilename());
      System.out.println("Test multipart - File size: " + file.getSize());
      System.out.println("Test multipart - Content type: " + file.getContentType());
      
      response.put("success", true);
      response.put("fileName", file.getOriginalFilename());
      response.put("fileSize", file.getSize());
      response.put("contentType", file.getContentType());
      response.put("message", "Test multipart thành công");
    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "Test multipart thất bại: " + e.getMessage());
    }
    
    return response;
  }

  @PostMapping("/login")
  public Map<String, Object> login(@RequestBody LoginAccountDto dto, HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    if (dto.getLoginIdentifier().isEmpty() || dto.getPassword().isEmpty()) {
      response.put("message", "Vui lòng điền đầy đủ thông tin đăng nhập!");
      return response;
    }
    
    // Tìm account theo username hoặc email
    Account account = null;
    String loginIdentifier = dto.getLoginIdentifier().trim();
    
    // Kiểm tra xem có phải email không
    if (loginIdentifier.contains("@")) {
      // Đăng nhập bằng email
      if (!accountRepository.existsByEmail(loginIdentifier)) {
        response.put("message", "Sai email hoặc mật khẩu!");
        return response;
      }
      account = accountRepository.findByEmailAndIsActive(loginIdentifier, true);
    } else {
      // Đăng nhập bằng username
      if (!accountRepository.existsByUsername(loginIdentifier)) {
        response.put("message", "Sai tên đăng nhập hoặc mật khẩu!");
        return response;
      }
      account = accountRepository.findByUsernameAndIsActive(loginIdentifier, true);
    }
    
    if (account == null) {
      response.put("message", "Tài khoản không tồn tại!");
      return response;
    } else if (!account.getIsActive()) {
      response.put("message", "Tài khoản của bạn đã bị khóa!");
      return response;
    } else if (!BCrypt.checkpw(dto.getPassword(), account.getPassword())) {
      response.put("message", "Sai thông tin đăng nhập hoặc mật khẩu!");
      return response;
    }
    session.setAttribute("account", account);
    return response;
  }

  @PostMapping("/google-login")
  public Map<String, Object> googleLogin(@RequestBody GoogleLoginDto dto, HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    
    try {
      System.out.println("Received Google login data: " + dto);
      System.out.println("Google name: '" + dto.getName() + "'");
      System.out.println("Google name bytes: " + java.util.Arrays.toString(dto.getName() != null ? dto.getName().getBytes(java.nio.charset.StandardCharsets.UTF_8) : "null".getBytes()));
      
      if (dto.getGoogleId() == null || dto.getGoogleId().isEmpty()) {
        response.put("message", "Thông tin Google không hợp lệ!");
        return response;
      }

      Account account = googleAuthService.authenticateGoogleUser(dto);
      session.setAttribute("account", account);
      
      System.out.println("Account name after authentication: '" + account.getName() + "'");
      System.out.println("Account name bytes: " + java.util.Arrays.toString(account.getName() != null ? account.getName().getBytes(java.nio.charset.StandardCharsets.UTF_8) : "null".getBytes()));
      
      response.put("message", "Đăng nhập Google thành công!");
      response.put("account", account);
      return response;
    } catch (Exception e) {
      System.err.println("Google login error: " + e.getMessage());
      e.printStackTrace();
      response.put("message", "Đăng nhập Google thất bại: " + e.getMessage());
      return response;
    }
  }

  @PostMapping("/register")
  public Map<String, Object> register(@RequestBody RegisterAccountDto dto) {
    Map<String, Object> response = new HashMap<>();
    if (dto.getName().isEmpty() || dto.getUsername().isEmpty() || dto.getEmail().isEmpty() ||
        dto.getPassword().isEmpty() || dto.getPhone().isEmpty()) {
      response.put("message", "Vui lòng điền đầy đủ thông tin!");
      return response;
    }
    if (accountRepository.existsByUsername(dto.getUsername())) {
      response.put("message", "Username đã được sử dụng!");
      return response;
    }
    if (accountRepository.existsByEmail(dto.getEmail())) {
      response.put("message", "Email đã được sử dụng!");
      return response;
    }
    // Kiểm tra độ mạnh mật khẩu
    PasswordStrengthService.PasswordStrengthResult strengthResult = 
        passwordStrengthService.checkPasswordStrength(dto.getPassword());
    
    if (!strengthResult.isValid()) {
      response.put("message", strengthResult.getMessage());
      response.put("strength", strengthResult.getStrength().name());
      response.put("score", strengthResult.getScore());
      return response;
    }
    if (accountRepository.existsByPhone(dto.getPhone())) {
      response.put("message", "Số điện thoại đã được sử dụng!");
      return response;
    }
    accountService.registerAccount(dto);
    response.put("message", "Đăng ký tài khoản thành công!");
    response.put("account", dto);
    return response;
  }

  // Admin endpoints
  @GetMapping("/admin/users")
  public Map<String, Object> getAllUsersForAdmin(
      @RequestParam(value = "search", required = false) String search,
      HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");

    if (account == null || !"ADMIN".equals(account.getRole())) {
      response.put("message", "Bạn không có quyền truy cập!");
      return response;
    }

    try {
      List<Account> users;
      
      // Nếu có tham số tìm kiếm, tìm kiếm theo username hoặc email
      if (search != null && !search.trim().isEmpty()) {
        users = accountRepository.findByUsernameOrEmailContaining(search.trim());
      } else {
        users = accountRepository.findAllExceptAdmin();
      }
      
      List<Map<String, Object>> userList = new ArrayList<>();

      for (Account user : users) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("username", user.getUsername());
        userData.put("name", user.getName());
        userData.put("email", user.getEmail());
        userData.put("phone", user.getPhone());
        userData.put("role", user.getRole());
        userData.put("isActive", user.getIsActive());
        userData.put("createdAt", user.getCreatedAt());
        userData.put("lastLogin", user.getLastLogin());
        userList.add(userData);
      }

      response.put("users", userList);
      response.put("totalCount", userList.size());
      if (search != null && !search.trim().isEmpty()) {
        response.put("searchTerm", search.trim());
      }
    } catch (Exception e) {
      response.put("message", "Lỗi khi tải danh sách người dùng: " + e.getMessage());
    }

    return response;
  }

  @PutMapping("/admin/users/{userId}/role")
  public Map<String, Object> updateUserRole(@PathVariable String userId, @RequestBody Map<String, String> requestBody,
      HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");

    if (account == null || !"ADMIN".equals(account.getRole())) {
      response.put("message", "Bạn không có quyền truy cập!");
      return response;
    }

    try {
      UUID uuid = UUID.fromString(userId);
      String newRole = requestBody.get("role");

      Optional<Account> userOptional = accountRepository.findById(uuid);
      if (!userOptional.isPresent()) {
        response.put("message", "Không tìm thấy người dùng!");
        return response;
      }

      Account user = userOptional.get();
      user.setRole(newRole);
      accountRepository.save(user);

      response.put("message", "Cập nhật vai trò thành công!");
    } catch (Exception e) {
      response.put("message", "Lỗi khi cập nhật vai trò: " + e.getMessage());
    }

    return response;
  }

  @PutMapping("/admin/users/{userId}/status")
  public Map<String, Object> updateUserStatus(@PathVariable String userId,
      @RequestBody Map<String, Boolean> requestBody, HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");

    if (account == null || !"ADMIN".equals(account.getRole())) {
      response.put("message", "Bạn không có quyền truy cập!");
      return response;
    }

    try {
      UUID uuid = UUID.fromString(userId);
      Boolean isActive = requestBody.get("isActive");

      Optional<Account> userOptional = accountRepository.findById(uuid);
      if (!userOptional.isPresent()) {
        response.put("message", "Không tìm thấy người dùng!");
        return response;
      }

      Account user = userOptional.get();
      user.setIsActive(isActive);
      accountRepository.save(user);

      response.put("message", "Cập nhật trạng thái thành công!");
    } catch (Exception e) {
      response.put("message", "Lỗi khi cập nhật trạng thái: " + e.getMessage());
    }

    return response;
  }

  @PutMapping("/account/update")
  public ResponseEntity<Map<String, Object>> updateAccount(@RequestBody UpdateAccountDto updateDto, HttpSession session) {
    try {
      Account account = (Account) session.getAttribute("account");
      if (account == null) {
        Map<String, Object> response = Map.of(
          "success", false,
          "message", "Bạn chưa đăng nhập!"
        );
        return ResponseEntity.badRequest().body(response);
      }
      
      // Log để debug encoding
      System.out.println("Update account - Received name: '" + updateDto.getName() + "'");
      System.out.println("Update account - Name bytes: " + java.util.Arrays.toString(updateDto.getName() != null ? updateDto.getName().getBytes(java.nio.charset.StandardCharsets.UTF_8) : "null".getBytes()));
      
      Account updatedAccount = accountService.updateAccount(updateDto);
      session.setAttribute("account", updatedAccount);
      
      // Log sau khi update
      System.out.println("Update account - Updated name: '" + updatedAccount.getName() + "'");
      System.out.println("Update account - Updated name bytes: " + java.util.Arrays.toString(updatedAccount.getName() != null ? updatedAccount.getName().getBytes(java.nio.charset.StandardCharsets.UTF_8) : "null".getBytes()));
      
      Map<String, Object> response = Map.of(
        "success", true,
        "message", "Cập nhật thông tin thành công"
      );
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> response = Map.of(
        "success", false,
        "message", e.getMessage()
      );
      return ResponseEntity.badRequest().body(response);
    }
  }

  @PutMapping("/account/update-profile-image")
  public ResponseEntity<Map<String, Object>> updateProfileImage(@RequestParam("profileImage") MultipartFile profileImage, HttpSession session) {
    try {
      Account account = (Account) session.getAttribute("account");
      if (account == null) {
        Map<String, Object> response = Map.of(
          "success", false,
          "message", "Bạn chưa đăng nhập!"
        );
        return ResponseEntity.badRequest().body(response);
      }

      if (profileImage == null || profileImage.isEmpty()) {
        Map<String, Object> response = Map.of(
          "success", false,
          "message", "Vui lòng chọn ảnh để tải lên!"
        );
        return ResponseEntity.badRequest().body(response);
      }

      // Validate file type
      String contentType = profileImage.getContentType();
      if (contentType == null || !contentType.startsWith("image/")) {
        Map<String, Object> response = Map.of(
          "success", false,
          "message", "File không phải là ảnh hợp lệ!"
        );
        return ResponseEntity.badRequest().body(response);
      }

      // Validate file size (5MB)
      if (profileImage.getSize() > 5 * 1024 * 1024) {
        Map<String, Object> response = Map.of(
          "success", false,
          "message", "Kích thước file không được vượt quá 5MB!"
        );
        return ResponseEntity.badRequest().body(response);
      }

      // Upload image to Cloudinary
      String imageUrl = imageUploadService.uploadImage(profileImage);
      
      if (imageUrl == null || imageUrl.isEmpty()) {
        Map<String, Object> response = Map.of(
          "success", false,
          "message", "Không thể upload ảnh lên Cloudinary!"
        );
        return ResponseEntity.badRequest().body(response);
      }
      
      // Update account with new profile image
      account.setAccountsImage(imageUrl);
      accountRepository.save(account);
      
      // Update session
      session.setAttribute("account", account);
      
      Map<String, Object> response = Map.of(
        "success", true,
        "message", "Cập nhật ảnh đại diện thành công",
        "imageUrl", imageUrl
      );
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      System.err.println("Error updating profile image: " + e.getMessage());
      e.printStackTrace();
      Map<String, Object> response = Map.of(
        "success", false,
        "message", "Có lỗi xảy ra khi cập nhật ảnh: " + e.getMessage()
      );
      return ResponseEntity.badRequest().body(response);
    }
  }
}
