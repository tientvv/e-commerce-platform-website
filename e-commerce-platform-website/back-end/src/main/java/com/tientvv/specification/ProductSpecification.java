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
                
                // Search trong name - sử dụng function để chuyển đổi NCLOB
                Predicate namePredicate = criteriaBuilder.like(
                    criteriaBuilder.function("LOWER", String.class, 
                        criteriaBuilder.function("TO_CHAR", String.class, root.get("name"))
                    ), 
                    searchTerm
                );
                
                // Search trong brand - sử dụng function để chuyển đổi NCLOB
                Predicate brandPredicate = criteriaBuilder.like(
                    criteriaBuilder.function("LOWER", String.class, 
                        criteriaBuilder.function("TO_CHAR", String.class, root.get("brand"))
                    ), 
                    searchTerm
                );
                
                // Search trong description - sử dụng function để chuyển đổi NCLOB
                Predicate descriptionPredicate = criteriaBuilder.like(
                    criteriaBuilder.function("LOWER", String.class, 
                        criteriaBuilder.function("TO_CHAR", String.class, root.get("description"))
                    ), 
                    searchTerm
                );
                
                // Search trong category name
                Predicate categoryPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.join("category").get("name")), 
                    searchTerm
                );
                
                // Kết hợp tất cả điều kiện search với OR
                Predicate searchPredicate = criteriaBuilder.or(
                    namePredicate, 
                    brandPredicate, 
                    descriptionPredicate, 
                    categoryPredicate
                );
                
                predicates.add(searchPredicate);
            } else {
                // Nếu query rỗng, trả về tất cả sản phẩm active
                return criteriaBuilder.isTrue(root.get("isActive"));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
