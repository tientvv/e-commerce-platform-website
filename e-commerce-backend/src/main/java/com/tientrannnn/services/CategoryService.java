package com.tientrannnn.services;

import com.tientrannnn.dtos.CategoryController.CategoryRequest;
import com.tientrannnn.dtos.CategoryController.CategoryResponse;
import com.tientrannnn.models.Category;
import com.tientrannnn.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CategoryService {

  @Autowired
  private CategoryRepository categoryRepository;

  public List<CategoryResponse> getAllCategories() {
    return categoryRepository.findAllActive().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  public List<CategoryResponse> getRootCategories() {
    return categoryRepository.findRootCategories().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  public CategoryResponse getCategoryById(UUID id) {
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Category not found"));
    if (category.getIsDeleted()) {
      throw new RuntimeException("Category not found");
    }
    return mapToResponse(category);
  }

  public CategoryResponse createCategory(CategoryRequest request) {
    Category category = new Category();
    category.setName(request.getName());
    category.setCategoryImage(request.getCategoryImage());
    category.setCreatedAt(OffsetDateTime.now());
    category.setUpdatedAt(OffsetDateTime.now());
    category.setIsDeleted(false);

    // Debug log
    System.out.println("Creating category with image: " + request.getCategoryImage());

    if (request.getParentId() != null) {
      Category parent = categoryRepository.findById(request.getParentId())
          .orElseThrow(() -> new RuntimeException("Parent category not found"));
      category.setParent(parent);
    }

    Category saved = categoryRepository.save(category);
    System.out.println("Category saved with image: " + saved.getCategoryImage());
    return mapToResponse(saved);
  }

  public CategoryResponse updateCategory(UUID id, CategoryRequest request) {
    System.out.println("=== UPDATE CATEGORY DEBUG ===");
    System.out.println("Category ID: " + id);
    System.out.println("Request: " + request);
    System.out.println("Request categoryImage: " + request.getCategoryImage());

    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Category not found"));

    System.out.println("Found category: " + category.getName());
    System.out.println("Old categoryImage: " + category.getCategoryImage());

    category.setName(request.getName());
    category.setCategoryImage(request.getCategoryImage());
    category.setUpdatedAt(OffsetDateTime.now());

    System.out.println("Setting new categoryImage: " + request.getCategoryImage());

    if (request.getParentId() != null) {
      Category parent = categoryRepository.findById(request.getParentId())
          .orElseThrow(() -> new RuntimeException("Parent category not found"));
      category.setParent(parent);
    } else {
      category.setParent(null);
    }

    Category updated = categoryRepository.save(category);
    System.out.println("Saved category with image: " + updated.getCategoryImage());
    System.out.println("=== END UPDATE CATEGORY DEBUG ===");
    return mapToResponse(updated);
  }

  public void deleteCategory(UUID id) {
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Category not found"));

    category.setIsDeleted(true);
    category.setUpdatedAt(OffsetDateTime.now());
    categoryRepository.save(category);
  }

  private CategoryResponse mapToResponse(Category category) {
    CategoryResponse response = new CategoryResponse();
    response.setId(category.getId());
    response.setName(category.getName());
    response.setCategoryImage(category.getCategoryImage());
    response.setCreatedAt(category.getCreatedAt());
    response.setUpdatedAt(category.getUpdatedAt());

    // Debug log
    System.out.println("Mapping category " + category.getName() + " with image: " + category.getCategoryImage());

    if (category.getParent() != null) {
      response.setParentId(category.getParent().getId());
      response.setParentName(category.getParent().getName());
    }

    // Map children if needed
    if (category.getCategories() != null && !category.getCategories().isEmpty()) {
      List<CategoryResponse> children = category.getCategories().stream()
          .filter(child -> !child.getIsDeleted())
          .map(this::mapToResponse)
          .collect(Collectors.toList());
      response.setChildren(children);
    }

    return response;
  }
}
