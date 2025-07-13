package com.tientv.service;

import com.tientv.dto.CategoryDto;
import com.tientv.dto.CategoryParentDto;
import com.tientv.dto.CategoryResponse;
import com.tientv.dto.SelectCategory;
import com.tientv.model.Category;
import com.tientv.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional
    @CacheEvict(value = {"categories", "categoryStats"}, allEntries = true)
    public CategoryResponse createCategory(CategoryDto categoryDto) {
        // Kiểm tra tên category đã tồn tại chưa
        if (categoryRepository.existsByNameIgnoreCase(categoryDto.getName())) {
            throw new RuntimeException("Tên danh mục đã tồn tại! Vui lòng chọn tên khác.");
        }

        Category category = new Category();
        category.setName(categoryDto.getName());
        category.setImageUrl(categoryDto.getImageUrl());
        category.setCreatedAt(OffsetDateTime.now());
        category.setUpdatedAt(OffsetDateTime.now());

        if (categoryDto.getParentId() != null) {
            Category parent = categoryRepository.findById(categoryDto.getParentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục cha"));
            category.setParent(parent);
        }

        Category savedCategory = categoryRepository.save(category);
        return convertToResponse(savedCategory);
    }

    @Cacheable(value = "categories", key = "'select'")
    public List<SelectCategory> getSelectCategory() {
        return categoryRepository.findAll().stream()
                .map(category -> new SelectCategory(category.getId(), category.getName()))
                .collect(Collectors.toList());
    }

    // FIX: Không sử dụng Sort trong Pageable để tránh duplicate ORDER BY
    public Page<Category> getCategories(int page, int size) {
        Pageable pageable = PageRequest.of(page, size); // Không có Sort
        return categoryRepository.findAllWithParent(pageable);
    }

    public List<CategoryResponse> toCategoryResponse(List<Category> categories) {
        return categories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = {"categories", "categoryStats"}, allEntries = true)
    public CategoryResponse updateCategory(UUID id, CategoryDto dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));

        // Kiểm tra tên trùng (trừ chính category đang update)
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(dto.getName(), id)) {
            throw new RuntimeException("Tên danh mục đã tồn tại! Vui lòng chọn tên khác.");
        }

        category.setName(dto.getName());
        category.setImageUrl(dto.getImageUrl());
        category.setUpdatedAt(OffsetDateTime.now());

        if (dto.getParentId() != null) {
            // Kiểm tra tránh vòng lặp vô hạn - category không thể là parent của chính nó
            if (dto.getParentId().equals(id)) {
                throw new RuntimeException("Danh mục không thể là danh mục cha của chính nó!");
            }

            Category parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục cha"));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        Category savedCategory = categoryRepository.save(category);
        return convertToResponse(savedCategory);
    }

    @Transactional
    @CacheEvict(value = {"categories", "categoryStats"}, allEntries = true)
    public void deleteCategory(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));

        // Kiểm tra xem category này có sản phẩm không
        if (categoryRepository.hasProducts(id)) {
            throw new RuntimeException("Không thể xóa danh mục này vì đang có sản phẩm!");
        }

        // Bước 1: Set parent_id = NULL cho tất cả children categories
        List<Category> childCategories = categoryRepository.findByParentId(id);
        if (!childCategories.isEmpty()) {
            for (Category child : childCategories) {
                child.setParent(null);
                child.setUpdatedAt(OffsetDateTime.now());
            }
            categoryRepository.saveAll(childCategories);
        }

        // Bước 2: Xóa category
        categoryRepository.delete(category);
    }

    @Cacheable(value = "categoryStats", key = "'total'")
    public long getTotalCategories() {
        return categoryRepository.count();
    }

    @Cacheable(value = "categoryStats", key = "'parent'")
    public long getTotalParentCategories() {
        return categoryRepository.countByParentIsNull();
    }

    @Cacheable(value = "categoryStats", key = "'child'")
    public long getTotalChildCategories() {
        return categoryRepository.countByParentIsNotNull();
    }

    // Helper method để convert Entity sang DTO
    private CategoryResponse convertToResponse(Category category) {
        CategoryResponse dto = new CategoryResponse();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setImageUrl(category.getImageUrl());

        // Safe access parent
        if (category.getParent() != null) {
            CategoryParentDto parentDto = new CategoryParentDto();
            parentDto.setId(category.getParent().getId());
            parentDto.setName(category.getParent().getName());
            parentDto.setImageUrl(category.getParent().getImageUrl());
            dto.setParent(parentDto);
        }

        return dto;
    }
}