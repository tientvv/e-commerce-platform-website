package com.tientvv.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tientvv.dto.Categories.CategoryDto;
import com.tientvv.repository.CategoryRepository;

@Service
public class CategoryService {

  @Autowired
  private CategoryRepository categoryRepository;

  public List<CategoryDto> getAllCategories() {
    return categoryRepository.findAll().stream()
        .map(category -> new CategoryDto(category.getId(), category.getName()))
        .collect(Collectors.toList());
  }
}
