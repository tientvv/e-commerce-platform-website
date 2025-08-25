package com.tientvv.service;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.cloudinary.Cloudinary;

@Service
public class ImageUploadService {

  @Autowired
  private Cloudinary cloudinary;

  public String uploadImage(MultipartFile file) throws Exception {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File không được để trống");
    }
    
    try {
      Map<?, ?> uploadResult = cloudinary.uploader().upload(
          file.getBytes(),
          Map.of("resource_type", "image")
      );
      
      String secureUrl = (String) uploadResult.get("secure_url");
      if (secureUrl == null || secureUrl.isEmpty()) {
        throw new RuntimeException("Không thể lấy URL ảnh từ Cloudinary");
      }
      
      return secureUrl;
    } catch (Exception e) {
      System.err.println("Error uploading image to Cloudinary: " + e.getMessage());
      throw new RuntimeException("Lỗi khi upload ảnh lên Cloudinary: " + e.getMessage(), e);
    }
  }
}
