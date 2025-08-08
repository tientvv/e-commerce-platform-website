package com.tientvv.controller;

import java.util.*;
import org.springframework.web.bind.annotation.*;
import com.tientvv.dto.account.*;
import com.tientvv.model.Account;
import com.tientvv.repository.AccountRepository;
import com.tientvv.service.AccountService;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api")
public class AccountController {

  @Autowired
  private AccountService accountService;

  @Autowired
  private AccountRepository accountRepository;

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
    accountInfo.put("createdAt", account.getCreatedAt());
    accountInfo.put("updatedAt", account.getUpdatedAt());
    accountInfo.put("lastLogin", account.getLastLogin());
    accountInfo.put("isActive", account.getIsActive());
    
    response.put("account", accountInfo);
    return response;
  }

  @PostMapping("/login")
  public Map<String, Object> login(@RequestBody LoginAccountDto dto, HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    if (dto.getUsername().isEmpty() || dto.getPassword().isEmpty()) {
      response.put("message", "Vui lòng điền đầy đủ thông tin đăng nhập!");
      return response;
    }
    if (!accountRepository.existsByUsername(dto.getUsername())) {
      response.put("message", "Sai tên đăng nhập hoặc mật khẩu!");
      return response;
    }
    Account account = accountRepository.findByUsernameAndIsActive(dto.getUsername(), true);
    if (account == null) {
      response.put("message", "Tài khoản không tồn tại!");
      return response;
    } else if (!account.getIsActive()) {
      response.put("message", "Tài khoản của bạn đã bị khóa!");
      return response;
    } else if (!BCrypt.checkpw(dto.getPassword(), account.getPassword())) {
      response.put("message", "Sai tên đăng nhập hoặc mật khẩu!");
      return response;
    }
    session.setAttribute("account", account);
    return response;
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
    if (dto.getPassword().length() < 6) {
      response.put("message", "Mật khẩu phải có ít nhất 6 ký tự!");
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
  public Map<String, Object> getAllUsersForAdmin(HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");

    if (account == null || !"ADMIN".equals(account.getRole())) {
      response.put("message", "Bạn không có quyền truy cập!");
      return response;
    }

    try {
      List<Account> users = accountRepository.findAll();
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
      
      Account updatedAccount = accountService.updateAccount(updateDto);
      session.setAttribute("account", updatedAccount);
      
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
}
