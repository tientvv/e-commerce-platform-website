package com.tientrannnn.controllers;

import com.tientrannnn.dtos.CategoryController.CategoryRequest;
import com.tientrannnn.dtos.CategoryController.CategoryResponse;
import com.tientrannnn.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

  @Autowired
  private CategoryService categoryService;

  @GetMapping
  public ResponseEntity<List<CategoryResponse>> getAllCategories() {
    try {
      List<CategoryResponse> categories = categoryService.getAllCategories();
      return ResponseEntity.ok(categories);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GetMapping("/root")
  public ResponseEntity<List<CategoryResponse>> getRootCategories() {
    try {
      List<CategoryResponse> categories = categoryService.getRootCategories();
      return ResponseEntity.ok(categories);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable UUID id) {
    try {
      CategoryResponse category = categoryService.getCategoryById(id);
      return ResponseEntity.ok(category);
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PostMapping
  public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryRequest request) {
    try {
      System.out.println("Received create request with data: " + request);
      System.out.println("Category image in request: " + request.getCategoryImage());
      CategoryResponse category = categoryService.createCategory(request);
      return ResponseEntity.status(HttpStatus.CREATED).body(category);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<CategoryResponse> updateCategory(@PathVariable UUID id, @RequestBody CategoryRequest request) {
    try {
      System.out.println("Received update request for category " + id + " with data: " + request);
      System.out.println("Category image in request: " + request.getCategoryImage());
      CategoryResponse category = categoryService.updateCategory(id, request);
      return ResponseEntity.ok(category);
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
    try {
      categoryService.deleteCategory(id);
      return ResponseEntity.noContent().build();
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GetMapping("/parent/{parentId}")
  public ResponseEntity<List<CategoryResponse>> getCategoriesByParent(@PathVariable UUID parentId) {
    try {
      CategoryResponse parent = categoryService.getCategoryById(parentId);
      List<CategoryResponse> children = parent.getChildren() != null ? parent.getChildren() : List.of();
      return ResponseEntity.ok(children);
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
