package com.tientv.service;

import com.tientv.dto.CategoryDto;
import com.tientv.dto.CategoryParentDto;
import com.tientv.dto.CategoryResponse;
import com.tientv.dto.SelectCategory;
import com.tientv.model.Category;
import com.tientv.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private  CategoryRepository categoryRepository;

    public Category createCategory(CategoryDto categoryDto) {
        Category category = new Category();

        category.setName(categoryDto.getName());
        category.setImageUrl(categoryDto.getImageUrl());
        category.setCreatedAt(OffsetDateTime.now());
        category.setUpdatedAt(OffsetDateTime.now());

        if (categoryDto.getParentId() != null) {
            Category parent = categoryRepository.findById(categoryDto.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));
            category.setParent(parent);
        }

        return categoryRepository.save(category);
    }

    public List<SelectCategory> getSelectCategory() {
        return categoryRepository.findAll().stream()
                .map(category -> new SelectCategory(category.getId(), category.getName()))
                .collect(Collectors.toList());
    }

    public Page<Category> getCategories(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return categoryRepository.findAll(pageable);
    }

    public List<CategoryResponse> toCategoryResponse(List<Category> categories) {
        return categories.stream().map(cat -> {
            CategoryResponse dto = new CategoryResponse();
            dto.setId(cat.getId());
            dto.setName(cat.getName());
            dto.setImageUrl(cat.getImageUrl());
            if (cat.getParent() != null) {
                CategoryParentDto parentDto = new CategoryParentDto();
                parentDto.setId(cat.getParent().getId());
                parentDto.setName(cat.getParent().getName());
                parentDto.setImageUrl(cat.getParent().getImageUrl());
                dto.setParent(parentDto);
            }
            return dto;
        }).collect(Collectors.toList());
    }

    public Category updateCategory(UUID id, CategoryDto dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        category.setName(dto.getName());
        category.setImageUrl(dto.getImageUrl());
        category.setUpdatedAt(OffsetDateTime.now());
        if (dto.getParentId() != null) {
            Category parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent not found"));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }
        return categoryRepository.save(category);
    }
}
