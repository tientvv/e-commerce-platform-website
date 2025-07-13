package com.tientv.controller;

import com.tientv.dto.CategoryDto;
import com.tientv.dto.CategoryResponse;
import com.tientv.dto.PageResponse;
import com.tientv.dto.SelectCategory;
import com.tientv.model.Category;
import com.tientv.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/select")
    public ResponseEntity<List<SelectCategory>> selectCategory() {
        return ResponseEntity.ok(categoryService.getSelectCategory());
    }

    @PostMapping("/add")
    public ResponseEntity<?> addCategory(@RequestBody CategoryDto categoryDto) {
        try {
            CategoryResponse category = categoryService.createCategory(categoryDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(category);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/list")
    public PageResponse<CategoryResponse> getCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Category> pageResult = categoryService.getCategories(page, size);
        List<CategoryResponse> content = categoryService.toCategoryResponse(pageResult.getContent());
        PageResponse<CategoryResponse> resp = new PageResponse<>();
        resp.setContent(content);
        resp.setTotalPages(pageResult.getTotalPages());
        resp.setTotalElements(pageResult.getTotalElements());
        resp.setPage(pageResult.getNumber());
        resp.setSize(pageResult.getSize());
        return resp;
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable UUID id, @RequestBody CategoryDto dto) {
        try {
            CategoryResponse category = categoryService.updateCategory(id, dto);
            return ResponseEntity.ok(category);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable UUID id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok(Map.of("message", "Xóa danh mục thành công!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getCategoryStats() {
        Map<String, Long> stats = Map.of(
                "total", categoryService.getTotalCategories(),
                "parent", categoryService.getTotalParentCategories(),
                "child", categoryService.getTotalChildCategories()
        );
        return ResponseEntity.ok(stats);
    }
}