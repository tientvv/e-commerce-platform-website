package com.tientvv.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class ImageUploadController {

    @Autowired
    private Cloudinary cloudinary;

    @PostMapping
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), Map.of());
            String url = (String) uploadResult.get("secure_url");
            return ResponseEntity.ok(Map.of("image", url));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("" + e.getMessage());
        }
    }
}
