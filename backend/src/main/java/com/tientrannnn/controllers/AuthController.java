package com.tientrannnn.controllers;

import java.util.HashMap;
import java.util.Map;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.tientrannnn.dtos.LoginRequest;
import com.tientrannnn.models.User;
import com.tientrannnn.repositories.UserRepository;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
public class AuthController {

  @Autowired
  private UserRepository userRepository;

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
    String email = request.getEmail();
    String password = request.getPassword();
    User user = userRepository.findByEmail(email);
    if (user == null) {
      return ResponseEntity.status(401).body("Email does not exist");
    }
    if (!BCrypt.checkpw(password, user.getPassword())) {
      return ResponseEntity.status(401).body("Incorrect password");
    }
    session.setAttribute("user", user);
    return ResponseEntity.ok("Login successful");
  }

  @GetMapping("/info-user")
  public ResponseEntity<?> getUser(HttpSession session) {
    User user = (User) session.getAttribute("user");
    if (user == null) {
      return ResponseEntity.status(401).body("User not authenticated");
    }
    Map<String, Object> response = new HashMap<>();
    response.put("user", user);
    return ResponseEntity.ok(response);
  }
}
