package com.tientrannnn.repositories;

import com.tientrannnn.models.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BrandRepository extends JpaRepository<Brand, UUID> {
    
    @Query("SELECT b FROM Brand b WHERE b.isDeleted = false")
    Page<Brand> findAllActive(Pageable pageable);
    
    @Query("SELECT b FROM Brand b WHERE b.isDeleted = false")
    List<Brand> findAllActive();
    
    @Query("SELECT b FROM Brand b WHERE b.isDeleted = false AND b.id = :id")
    Brand findByIdActive(@Param("id") UUID id);
    
    @Query("SELECT b FROM Brand b WHERE b.isDeleted = false AND LOWER(b.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Brand> findByNameContainingIgnoreCase(@Param("name") String name);
    
    @Query("SELECT b FROM Brand b WHERE b.isDeleted = false ORDER BY (SELECT COUNT(p) FROM Product p WHERE p.brand = b) DESC")
    List<Brand> findPopularBrands(Pageable pageable);
}