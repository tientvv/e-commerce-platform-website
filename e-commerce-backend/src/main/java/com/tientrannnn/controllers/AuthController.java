package com.tientrannnn.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.tientrannnn.dtos.AuthController.LoginRequest;
import com.tientrannnn.dtos.AuthController.SessionResponse;
import com.tientrannnn.models.User;
import com.tientrannnn.repositories.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  @Autowired
  private UserRepository UserRepository;

  @GetMapping("info-account")
  public ResponseEntity<?> infoAccount(HttpSession session) {
    User user = (User) session.getAttribute("user");
    if (user == null) {
      return ResponseEntity.status(401).body("Unauthorized");
    }
    return ResponseEntity.ok(new SessionResponse(user));
  }

  @PostMapping("login")
  public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
    String email = request.getEmail();
    String password = request.getPassword();
    User user = UserRepository.findByEmail(email);
    if (user == null) {
      return ResponseEntity.status(401).body("Email does not exist");
    }
    if (!BCrypt.checkpw(password, user.getPassword())) {
      return ResponseEntity.status(401).body("Incorrect password");
    }
    session.setAttribute("user", user);
    return ResponseEntity.ok("Login successful");
  }

  @PostMapping("logout")
  public ResponseEntity<?> logout(HttpSession session) {
    session.removeAttribute("user");
    return ResponseEntity.ok("Logout successful");
  }
}
