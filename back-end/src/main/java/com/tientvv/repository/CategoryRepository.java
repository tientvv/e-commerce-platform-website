package com.tientvv.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.tientvv.model.Category;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
  boolean existsByName(String name);
}
