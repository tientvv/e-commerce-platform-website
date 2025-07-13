package com.tientv.repository;

import com.tientv.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    // Kiểm tra tên category đã tồn tại (không phân biệt hoa thường)
    boolean existsByNameIgnoreCase(String name);

    // Kiểm tra tên category đã tồn tại nhưng loại trừ ID hiện tại (dùng cho update)
    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    // Tìm tất cả children categories của một parent
    List<Category> findByParentId(UUID parentId);

    // Kiểm tra xem category có sản phẩm không
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Product p WHERE p.category.id = :categoryId")
    boolean hasProducts(@Param("categoryId") UUID categoryId);

    // Đếm tổng số danh mục cha (parent_id = null)
    long countByParentIsNull();

    // Đếm tổng số danh mục con (parent_id != null)
    long countByParentIsNotNull();

    // Find all với JOIN FETCH - FIX ORDER BY
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.parent ORDER BY c.createdAt DESC")
    List<Category> findAllWithParent();

    // Find với pagination và JOIN FETCH - KHÔNG dùng Sort trong Pageable
    @Query(value = "SELECT c FROM Category c LEFT JOIN FETCH c.parent ORDER BY c.createdAt DESC",
            countQuery = "SELECT COUNT(c) FROM Category c")
    Page<Category> findAllWithParent(Pageable pageable);
}