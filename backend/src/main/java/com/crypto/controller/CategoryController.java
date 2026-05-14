package com.crypto.controller;

import com.crypto.dto.request.CategoryRequest;
import com.crypto.dto.response.CategoryResponse;
import com.crypto.dto.response.ErrorResponse;
import com.crypto.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")

@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management endpoints")
public class CategoryController {
    
    private final CategoryService categoryService;
    
    @GetMapping
    @Operation(summary = "Get all categories")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<CategoryResponse>> getCategories() {
        return ResponseEntity.ok(categoryService.getUserCategories());
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CategoryResponse> getCategory(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }
    
    @PostMapping
    @Operation(summary = "Create new category")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest categoryRequest) {
        return ResponseEntity.ok(categoryService.createCategory(categoryRequest));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update category")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest categoryRequest) {
        return ResponseEntity.ok(categoryService.updateCategory(id, categoryRequest));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok().body(new ErrorResponse("Category deleted successfully"));
    }
    
    @GetMapping("/{id}/addresses")
    @Operation(summary = "Get addresses by category")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<CategoryResponse>> getCategoryAddresses(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryAddresses(id));
    }
}