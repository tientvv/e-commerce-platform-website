package com.tientvv.controller;

import java.util.*;
import org.springframework.web.bind.annotation.*;

import com.tientvv.dto.account.InfoAccountDto;
import com.tientvv.dto.account.LoginAccountDto;
import com.tientvv.dto.account.RegisterAccountDto;
import com.tientvv.dto.account.UpdateAccountDto;
import com.tientvv.model.Account;
import com.tientvv.repository.AccountRepository;
import com.tientvv.service.AccountService;

import jakarta.servlet.http.HttpSession;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;

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

  @PostMapping("/update-account")
  public Map<String, Object> updateAccount(@RequestBody UpdateAccountDto dto, HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    Account account = (Account) session.getAttribute("account");
    if (account == null) {
      response.put("message", "Bạn chưa đăng nhập!");
      return response;
    }
    Account updatedAccount = accountService.updateAccount(dto);
    session.setAttribute("account", updatedAccount);
    response.put("message", "Cập nhật tài khoản thành công!");
    response.put("account", new InfoAccountDto(updatedAccount.getUsername(), updatedAccount.getName(),
        updatedAccount.getEmail(), updatedAccount.getPhone(), updatedAccount.getRole()));
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
    response.put("account", new InfoAccountDto(account.getUsername(), account.getName(), account.getEmail(),
        account.getPhone(), account.getRole()));
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
}
