package com.tientv.service;

import java.time.OffsetDateTime;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tientv.dto.account.RegisterAccountDto;
import com.tientv.dto.account.UpdateAccountDto;
import com.tientv.model.Account;
import com.tientv.repository.AccountRepository;

@Service
public class AccountService {

  @Autowired
  private AccountRepository accountRepository;

  public Account updateAccount(UpdateAccountDto dto) {
    Account account = accountRepository.findByUsername(dto.getUsername());
    if (account != null) {
      account.setName(dto.getName());
      account.setEmail(dto.getEmail());
      account.setPhone(dto.getPhone());
      return accountRepository.save(account);
    }
    throw new RuntimeException("Không tìm thấy tài khoản với tên đăng nhập: " + dto.getUsername());
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
