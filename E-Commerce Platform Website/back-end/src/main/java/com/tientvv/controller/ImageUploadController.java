package com.tientvv.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tientvv.model.Account;
import com.tientvv.service.ImageUploadService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
public class ImageUploadController {

  @Autowired
  private ImageUploadService imageUploadService;

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

      // Kiểm tra file type
      String contentType = file.getContentType();
      if (contentType == null || !contentType.startsWith("image/")) {
        response.put("message", "File không phải là ảnh!");
        return response;
      }

      // Kiểm tra file size (max 10MB)
      if (file.getSize() > 10 * 1024 * 1024) {
        response.put("message", "File quá lớn! Vui lòng chọn file dưới 10MB.");
        return response;
      }

      String imageUrl = imageUploadService.uploadImage(file);
      response.put("success", true);
      response.put("imageUrl", imageUrl);
      response.put("message", "Upload ảnh thành công!");
    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "Lỗi khi upload ảnh: " + e.getMessage());
    }
    return response;
  }
}