package com.tientvv.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.tientvv.dto.Categories.CategoryDto;
import com.tientvv.service.CategoryService;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

  @Autowired
  private CategoryService categoryService;

  @GetMapping
  public List<CategoryDto> getAllCategories() {
    return categoryService.getAllCategories();
  }
}
