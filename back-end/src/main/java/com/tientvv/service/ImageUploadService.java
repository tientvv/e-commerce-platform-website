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
    if (file == null || file.isEmpty())
      return null;
    Map<?, ?> uploadResult = cloudinary.uploader().upload(
        file.getBytes(),
        Map.of()
    );
    return (String) uploadResult.get("secure_url");
  }
}
