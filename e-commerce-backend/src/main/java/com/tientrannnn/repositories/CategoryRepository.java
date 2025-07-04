package com.tientrannnn.repositories;

import com.tientrannnn.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

  @Query("SELECT c FROM Category c WHERE c.isDeleted = false")
  List<Category> findAllActive();

  @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.isDeleted = false")
  List<Category> findRootCategories();

  @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId AND c.isDeleted = false")
  List<Category> findByParentId(UUID parentId);
}
