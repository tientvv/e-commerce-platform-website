package com.tientrannnn.controllers;

import java.io.IOException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.tientrannnn.models.User;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
public class UploadController {

  @Autowired
  private Cloudinary cloudinary;

  @SuppressWarnings("unchecked")
  @PostMapping("/upload")
  public ResponseEntity<?> upload(@RequestParam("image") MultipartFile file, HttpSession session) throws IOException {
    User user = (User) session.getAttribute("user");
    if (user == null) {
      return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
    }

    Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
    String url = uploadResult.get("secure_url").toString();
    return ResponseEntity.ok(Map.of("imageUrl", url));
  }
}
