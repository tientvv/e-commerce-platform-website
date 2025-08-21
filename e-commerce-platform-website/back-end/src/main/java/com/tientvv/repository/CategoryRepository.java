package com.tientvv.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.tientvv.model.Category;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
  boolean existsByName(String name);
  
  // Kiểm tra trùng tên không phân biệt case và loại trừ danh mục hiện tại
  boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
  
  // Kiểm tra trùng tên không phân biệt case (cho việc tạo mới)
  boolean existsByNameIgnoreCase(String name);
}
