package com.tientvv.specification;

import org.springframework.data.jpa.domain.Specification;
import com.tientvv.model.Product;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> searchProducts(String query) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Chỉ tìm sản phẩm active
            predicates.add(criteriaBuilder.isTrue(root.get("isActive")));
            
            if (query != null && !query.trim().isEmpty()) {
                String searchTerm = "%" + query.toLowerCase() + "%";
                
                // Search trong category name (tránh NCLOB fields)
                Predicate categoryPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.join("category").get("name")), 
                    searchTerm
                );
                
                // Kết hợp điều kiện search
                Predicate searchPredicate = categoryPredicate;
                
                predicates.add(searchPredicate);
            } else {
                // Nếu query rỗng, trả về tất cả sản phẩm active
                return criteriaBuilder.isTrue(root.get("isActive"));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
