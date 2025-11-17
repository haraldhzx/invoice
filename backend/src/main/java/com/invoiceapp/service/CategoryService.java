package com.invoiceapp.service;

import com.invoiceapp.exception.ResourceNotFoundException;
import com.invoiceapp.exception.ValidationException;
import com.invoiceapp.model.dto.CategoryDto;
import com.invoiceapp.model.entity.Category;
import com.invoiceapp.model.entity.User;
import com.invoiceapp.model.enums.CategoryType;
import com.invoiceapp.repository.CategoryRepository;
import com.invoiceapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories(UUID userId) {
        log.debug("Fetching all categories for user: {}", userId);
        List<Category> categories = categoryRepository.findByUserIdOrUserIdIsNull(userId);
        return categories.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getCategoriesByType(UUID userId, CategoryType type) {
        log.debug("Fetching categories by type {} for user: {}", type, userId);
        List<Category> categories = categoryRepository.findAvailableCategories(userId, type);
        return categories.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(UUID id, UUID userId) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        // Ensure user can access this category
        if (category.getUser() != null && !category.getUser().getId().equals(userId)) {
            throw new ValidationException("You don't have access to this category");
        }

        return mapToDto(category);
    }

    @Transactional
    public CategoryDto createCategory(CategoryDto categoryDto, UUID userId) {
        log.info("Creating custom category for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Category category = Category.builder()
                .name(categoryDto.getName())
                .type(categoryDto.getType())
                .icon(categoryDto.getIcon())
                .color(categoryDto.getColor())
                .description(categoryDto.getDescription())
                .isCustom(true)
                .user(user)
                .build();

        // Set parent if provided
        if (categoryDto.getParentId() != null) {
            Category parent = categoryRepository.findById(categoryDto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryDto.getParentId()));
            category.setParent(parent);
        }

        category = categoryRepository.save(category);
        log.info("Custom category created with ID: {}", category.getId());

        return mapToDto(category);
    }

    @Transactional
    public CategoryDto updateCategory(UUID id, CategoryDto categoryDto, UUID userId) {
        log.info("Updating category: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        // Ensure user owns this category
        if (!category.getIsCustom() || category.getUser() == null || !category.getUser().getId().equals(userId)) {
            throw new ValidationException("You can only update your own custom categories");
        }

        category.setName(categoryDto.getName());
        category.setIcon(categoryDto.getIcon());
        category.setColor(categoryDto.getColor());
        category.setDescription(categoryDto.getDescription());

        // Update parent if provided
        if (categoryDto.getParentId() != null) {
            Category parent = categoryRepository.findById(categoryDto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryDto.getParentId()));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        category = categoryRepository.save(category);
        log.info("Category updated: {}", id);

        return mapToDto(category);
    }

    @Transactional
    public void deleteCategory(UUID id, UUID userId) {
        log.info("Deleting category: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        // Ensure user owns this category
        if (!category.getIsCustom() || category.getUser() == null || !category.getUser().getId().equals(userId)) {
            throw new ValidationException("You can only delete your own custom categories");
        }

        categoryRepository.delete(category);
        log.info("Category deleted: {}", id);
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getSubcategories(UUID parentId) {
        log.debug("Fetching subcategories for parent: {}", parentId);
        List<Category> subcategories = categoryRepository.findSubcategories(parentId);
        return subcategories.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private CategoryDto mapToDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .type(category.getType())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .icon(category.getIcon())
                .color(category.getColor())
                .isCustom(category.getIsCustom())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
