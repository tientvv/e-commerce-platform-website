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
    public ResponseEntity<Category> addCategory(@RequestBody CategoryDto categoryDto) {
        Category category = categoryService.createCategory(categoryDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
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
    public Category updateCategory(@PathVariable UUID id, @RequestBody CategoryDto dto) {
        return categoryService.updateCategory(id, dto);
    }
}
