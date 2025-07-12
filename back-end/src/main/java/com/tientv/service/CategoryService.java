package com.tientv.service;

import com.tientv.dto.CategoryDto;
import com.tientv.model.Category;
import com.tientv.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;

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
}
