package com.invoiceapp.service;

import com.invoiceapp.model.dto.CategoryDto;
import com.invoiceapp.model.entity.Category;
import com.invoiceapp.model.entity.CategoryType;
import com.invoiceapp.model.entity.User;
import com.invoiceapp.repository.CategoryRepository;
import com.invoiceapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CategoryService categoryService;

    private User testUser;
    private Category defaultCategory;
    private Category customCategory;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");

        // Default category (no user)
        defaultCategory = new Category();
        defaultCategory.setId(UUID.randomUUID());
        defaultCategory.setName("Food & Dining");
        defaultCategory.setType(CategoryType.EXPENSE);
        defaultCategory.setIcon("restaurant");
        defaultCategory.setColor("#FF6B6B");
        defaultCategory.setCustom(false);
        defaultCategory.setUser(null);

        // Custom user category
        customCategory = new Category();
        customCategory.setId(UUID.randomUUID());
        customCategory.setName("My Custom Category");
        customCategory.setType(CategoryType.EXPENSE);
        customCategory.setCustom(true);
        customCategory.setUser(testUser);
    }

    @Test
    void getAllCategories_ShouldReturnAllCategoriesForUser() {
        // Arrange
        List<Category> categories = Arrays.asList(defaultCategory, customCategory);
        when(categoryRepository.findByUserIdOrUserIdIsNull(testUser.getId())).thenReturn(categories);

        // Act
        List<CategoryDto> result = categoryService.getAllCategories(testUser.getId());

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(categoryRepository).findByUserIdOrUserIdIsNull(testUser.getId());
    }

    @Test
    void getCategoriesByType_ShouldReturnFilteredCategories() {
        // Arrange
        List<Category> expenseCategories = Arrays.asList(defaultCategory, customCategory);
        when(categoryRepository.findByTypeAndUserIdOrUserIdIsNull(CategoryType.EXPENSE, testUser.getId()))
                .thenReturn(expenseCategories);

        // Act
        List<CategoryDto> result = categoryService.getCategoriesByType(CategoryType.EXPENSE, testUser.getId());

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(c -> c.getType() == CategoryType.EXPENSE));
        verify(categoryRepository).findByTypeAndUserIdOrUserIdIsNull(CategoryType.EXPENSE, testUser.getId());
    }

    @Test
    void getCategoryById_WhenExists_ShouldReturnCategory() {
        // Arrange
        UUID categoryId = defaultCategory.getId();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(defaultCategory));

        // Act
        CategoryDto result = categoryService.getCategoryById(categoryId);

        // Assert
        assertNotNull(result);
        assertEquals(categoryId, result.getId());
        assertEquals("Food & Dining", result.getName());
        assertEquals("restaurant", result.getIcon());
        verify(categoryRepository).findById(categoryId);
    }

    @Test
    void getCategoryById_WhenNotExists_ShouldThrowException() {
        // Arrange
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> categoryService.getCategoryById(categoryId));
    }

    @Test
    void createCategory_WithValidDto_ShouldCreateAndReturnCategory() {
        // Arrange
        CategoryDto createDto = new CategoryDto();
        createDto.setName("New Custom Category");
        createDto.setType(CategoryType.EXPENSE);
        createDto.setIcon("custom");
        createDto.setColor("#00FF00");

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(categoryRepository.save(any(Category.class))).thenReturn(customCategory);

        // Act
        CategoryDto result = categoryService.createCategory(createDto, testUser.getId());

        // Assert
        assertNotNull(result);
        assertTrue(result.isCustom());
        verify(userRepository).findById(testUser.getId());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_ForNonExistentUser_ShouldThrowException() {
        // Arrange
        CategoryDto createDto = new CategoryDto();
        createDto.setName("New Category");

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> categoryService.createCategory(createDto, testUser.getId()));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void updateCategory_WithValidDto_ShouldUpdateAndReturnCategory() {
        // Arrange
        UUID categoryId = customCategory.getId();
        CategoryDto updateDto = new CategoryDto();
        updateDto.setName("Updated Category Name");
        updateDto.setIcon("new_icon");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(customCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(customCategory);

        // Act
        CategoryDto result = categoryService.updateCategory(categoryId, updateDto);

        // Assert
        assertNotNull(result);
        verify(categoryRepository).save(customCategory);
    }

    @Test
    void deleteCategory_WhenCustomAndBelongsToUser_ShouldDelete() {
        // Arrange
        UUID categoryId = customCategory.getId();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(customCategory));

        // Act
        categoryService.deleteCategory(categoryId, testUser.getId());

        // Assert
        verify(categoryRepository).deleteById(categoryId);
    }

    @Test
    void deleteCategory_WhenDefaultCategory_ShouldThrowException() {
        // Arrange
        UUID categoryId = defaultCategory.getId();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(defaultCategory));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> categoryService.deleteCategory(categoryId, testUser.getId()));
        verify(categoryRepository, never()).deleteById(any());
    }

    @Test
    void deleteCategory_WhenBelongsToOtherUser_ShouldThrowException() {
        // Arrange
        UUID otherUserId = UUID.randomUUID();
        UUID categoryId = customCategory.getId();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(customCategory));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> categoryService.deleteCategory(categoryId, otherUserId));
        verify(categoryRepository, never()).deleteById(any());
    }

    @Test
    void getSubcategories_ShouldReturnChildCategories() {
        // Arrange
        UUID parentId = defaultCategory.getId();
        Category subcategory = new Category();
        subcategory.setId(UUID.randomUUID());
        subcategory.setName("Fast Food");
        subcategory.setParent(defaultCategory);

        List<Category> subcategories = Arrays.asList(subcategory);
        when(categoryRepository.findByParentId(parentId)).thenReturn(subcategories);

        // Act
        List<CategoryDto> result = categoryService.getSubcategories(parentId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Fast Food", result.get(0).getName());
        verify(categoryRepository).findByParentId(parentId);
    }
}
