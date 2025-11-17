package com.invoiceapp.controller;

import com.invoiceapp.model.dto.CategoryDto;
import com.invoiceapp.model.entity.User;
import com.invoiceapp.model.enums.CategoryType;
import com.invoiceapp.repository.UserRepository;
import com.invoiceapp.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management endpoints")
public class CategoryController {

    private final CategoryService categoryService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get all categories", description = "Get all available categories (default + custom)")
    public ResponseEntity<List<CategoryDto>> getAllCategories(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        List<CategoryDto> categories = categoryService.getAllCategories(user.getId());
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get categories by type", description = "Get categories filtered by type (INCOME or EXPENSE)")
    public ResponseEntity<List<CategoryDto>> getCategoriesByType(
            @PathVariable CategoryType type,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        List<CategoryDto> categories = categoryService.getCategoriesByType(user.getId(), type);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Get a specific category by ID")
    public ResponseEntity<CategoryDto> getCategoryById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        CategoryDto category = categoryService.getCategoryById(id, user.getId());
        return ResponseEntity.ok(category);
    }

    @GetMapping("/{id}/subcategories")
    @Operation(summary = "Get subcategories", description = "Get all subcategories for a parent category")
    public ResponseEntity<List<CategoryDto>> getSubcategories(@PathVariable UUID id) {
        List<CategoryDto> subcategories = categoryService.getSubcategories(id);
        return ResponseEntity.ok(subcategories);
    }

    @PostMapping
    @Operation(summary = "Create custom category", description = "Create a new custom category")
    public ResponseEntity<CategoryDto> createCategory(
            @Valid @RequestBody CategoryDto categoryDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        CategoryDto created = categoryService.createCategory(categoryDto, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update category", description = "Update a custom category")
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody CategoryDto categoryDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        CategoryDto updated = categoryService.updateCategory(id, categoryDto, user.getId());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category", description = "Delete a custom category")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        categoryService.deleteCategory(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    private User getUserFromDetails(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
