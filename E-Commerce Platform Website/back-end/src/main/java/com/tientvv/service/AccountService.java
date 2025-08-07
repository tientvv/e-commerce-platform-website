package com.tientvv.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tientvv.dto.account.RegisterAccountDto;
import com.tientvv.dto.account.UpdateAccountDto;
import com.tientvv.model.Account;
import com.tientvv.repository.AccountRepository;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class AccountService {

  @Autowired
  private AccountRepository accountRepository;

  public Account getAccountById(UUID id) {
    return accountRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với ID: " + id));
  }

  public Account updateAccount(UpdateAccountDto dto) {
    Account account = accountRepository.findById(dto.getId())
        .orElseThrow(() -> new RuntimeException("Account not found"));

    account.setUsername(dto.getUsername());
    account.setEmail(dto.getEmail());
    account.setPhone(dto.getPhone());
    account.setAddress(dto.getAddress());
    account.setUpdatedAt(OffsetDateTime.now());

    return accountRepository.save(account);
  }

  public Account getCurrentUserFromSession() {
    try {
      // Get current user from session using HttpSession
      HttpSession session = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
          .getSession();
      Account account = (Account) session.getAttribute("account");

      if (account == null) {
        return null;
      }

      return account;
    } catch (Exception e) {
      // If session is not available, return null
      return null;
    }
  }

  public Account registerAccount(RegisterAccountDto dto) {
    Account account = new Account();
    account.setName(dto.getName());
    account.setUsername(dto.getUsername());
    account.setEmail(dto.getEmail());
    account.setPassword(BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt()));
    account.setPhone(dto.getPhone());
    account.setIsActive(true);
    account.setRole("USER");
    account.setCreatedAt(OffsetDateTime.now());
    return accountRepository.save(account);
  }
}
