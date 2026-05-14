package com.crypto.service;

import com.crypto.dto.request.CategoryRequest;
import com.crypto.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getUserCategories();
    CategoryResponse getCategoryById(Long id);
    CategoryResponse createCategory(CategoryRequest categoryRequest);
    CategoryResponse updateCategory(Long id, CategoryRequest categoryRequest);
    void deleteCategory(Long id);
    List<CategoryResponse> getCategoryAddresses(Long id);
}