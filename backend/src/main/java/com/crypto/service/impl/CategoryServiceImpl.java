package com.crypto.service.impl;

import com.crypto.dto.request.CategoryRequest;
import com.crypto.dto.response.CategoryResponse;
import com.crypto.exception.ResourceNotFoundException;
import com.crypto.model.Category;
import com.crypto.model.User;
import com.crypto.repository.CategoryRepository;
import com.crypto.repository.UserRepository;
import com.crypto.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    
    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    @Override
    @Cacheable(value = "categories")
    public List<CategoryResponse> getUserCategories() {
        User currentUser = getCurrentUser();
        return categoryRepository.findByUserId(currentUser.getId()).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Cacheable(value = "categories", key = "#id")
    public CategoryResponse getCategoryById(Long id) {
        User currentUser = getCurrentUser();
        Category category = categoryRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return convertToResponse(category);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse createCategory(CategoryRequest categoryRequest) {
        User currentUser = getCurrentUser();
        
        if (categoryRepository.existsByNameAndUserId(categoryRequest.getName(), currentUser.getId())) {
            throw new IllegalArgumentException("Category with this name already exists");
        }
        
        Category category = Category.builder()
                .name(categoryRequest.getName())
                .description(categoryRequest.getDescription())
                .color(categoryRequest.getColor() != null ? categoryRequest.getColor() : "#6366F1")
                .user(currentUser)
                .build();
        
        Category savedCategory = categoryRepository.save(category);
        
        log.info("Category created: {} for user: {}", savedCategory.getName(), currentUser.getUsername());
        
        return convertToResponse(savedCategory);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "categories", key = "#id", allEntries = true)
    public CategoryResponse updateCategory(Long id, CategoryRequest categoryRequest) {
        User currentUser = getCurrentUser();
        Category category = categoryRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        
        // Check if name is being changed and if it already exists
        if (!category.getName().equals(categoryRequest.getName()) &&
                categoryRepository.existsByNameAndUserId(categoryRequest.getName(), currentUser.getId())) {
            throw new IllegalArgumentException("Category with this name already exists");
        }
        
        category.setName(categoryRequest.getName());
        category.setDescription(categoryRequest.getDescription());
        category.setColor(categoryRequest.getColor() != null ? categoryRequest.getColor() : category.getColor());
        
        Category updatedCategory = categoryRepository.save(category);
        
        log.info("Category updated: {} for user: {}", updatedCategory.getName(), currentUser.getUsername());
        
        return convertToResponse(updatedCategory);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public void deleteCategory(Long id) {
        User currentUser = getCurrentUser();
        Category category = categoryRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        
        categoryRepository.delete(category);
        
        log.info("Category deleted: {} for user: {}", category.getName(), currentUser.getUsername());
    }
    
    @Override
    public List<CategoryResponse> getCategoryAddresses(Long id) {
        // Implementation for getting addresses by category
        return List.of();
    }
    
    private CategoryResponse convertToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .color(category.getColor())
                .createdAt(category.getCreatedAt())
                .addressCount(category.getAddresses() != null ? category.getAddresses().size() : 0)
                .build();
    }
}