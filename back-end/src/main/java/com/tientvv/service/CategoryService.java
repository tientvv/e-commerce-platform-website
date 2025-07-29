package com.tientvv.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tientvv.dto.Categories.CategoryDto;
import com.tientvv.dto.Categories.CreateCategoryDto;
import com.tientvv.dto.Categories.UpdateCategoryDto;
import com.tientvv.model.Category;
import com.tientvv.repository.CategoryRepository;

@Service
public class CategoryService {

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private ImageUploadService imageUploadService;

  public List<CategoryDto> getAllCategories() {
    return categoryRepository.findAll().stream()
        .filter(category -> category.getIsActive() != null && category.getIsActive())
        .map(category -> new CategoryDto(category.getId(), category.getName(), category.getCategoryImage()))
        .collect(Collectors.toList());
  }

  public CategoryDto getCategoryById(UUID id) {
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    return new CategoryDto(category.getId(), category.getName(), category.getCategoryImage());
  }

  public Category createCategory(CreateCategoryDto dto) throws Exception {
    // Check if category name already exists
    if (categoryRepository.existsByName(dto.getName())) {
      throw new RuntimeException("Category name already exists: " + dto.getName());
    }

    Category category = new Category();
    category.setName(dto.getName());
    category.setIsActive(true);

    // Upload image if provided
    if (dto.getCategoryImage() != null && !dto.getCategoryImage().isEmpty()) {
      String imageUrl = imageUploadService.uploadImage(dto.getCategoryImage());
      category.setCategoryImage(imageUrl);
    }

    return categoryRepository.save(category);
  }

  public Category updateCategory(UpdateCategoryDto dto) throws Exception {
    Category category = categoryRepository.findById(dto.getId())
        .orElseThrow(() -> new RuntimeException("Category not found with id: " + dto.getId()));

    // Check if new name conflicts with existing categories (excluding current one)
    if (!category.getName().equals(dto.getName()) && categoryRepository.existsByName(dto.getName())) {
      throw new RuntimeException("Category name already exists: " + dto.getName());
    }

    category.setName(dto.getName());

    // Handle image update
    if (dto.getCategoryImage() != null && !dto.getCategoryImage().isEmpty()) {
      String imageUrl = imageUploadService.uploadImage(dto.getCategoryImage());
      category.setCategoryImage(imageUrl);
    } else if ("true".equals(dto.getRemoveImage())) {
      category.setCategoryImage(null);
    } else if (dto.getExistingImageUrl() != null) {
      category.setCategoryImage(dto.getExistingImageUrl());
    }

    return categoryRepository.save(category);
  }

  public void deleteCategory(UUID id) {
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

    // Check if category has products
    if (!category.getProducts().isEmpty()) {
      throw new RuntimeException(
          "Không thể xóa danh mục có sản phẩm. Vui lòng di chuyển sản phẩm sang danh mục khác trước.");
    }

    // Hard delete the category
    categoryRepository.delete(category);
  }

  public void hardDeleteCategory(UUID id) {
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

    // Check if category has products
    if (!category.getProducts().isEmpty()) {
      throw new RuntimeException(
          "Cannot delete category with existing products. Please move products to another category first.");
    }

    categoryRepository.delete(category);
  }
}
