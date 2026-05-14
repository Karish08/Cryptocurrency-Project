package com.crypto.repository;

import com.crypto.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    List<Category> findByUserId(Long userId);
    
    Optional<Category> findByIdAndUserId(Long id, Long userId);
    
    Optional<Category> findByNameAndUserId(String name, Long userId);
    
    boolean existsByNameAndUserId(String name, Long userId);
    
    void deleteByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    @org.springframework.data.jpa.repository.Query(value = "SELECT c.* FROM categories c LEFT JOIN address_categories ac ON c.id = ac.category_id JOIN addresses a ON ac.address_id = a.id WHERE c.user_id = :userId GROUP BY c.id ORDER BY COUNT(a.id) DESC LIMIT 5", nativeQuery = true)
    List<Category> findTop5ByUserIdOrderByAddressCountDesc(@org.springframework.data.repository.query.Param("userId") Long userId);
}