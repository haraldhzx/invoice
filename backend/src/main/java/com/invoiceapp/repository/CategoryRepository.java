package com.invoiceapp.repository;

import com.invoiceapp.model.entity.Category;
import com.invoiceapp.model.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findByType(CategoryType type);

    List<Category> findByUserIdOrUserIdIsNull(UUID userId);

    List<Category> findByTypeAndUserIdOrTypeAndUserIdIsNull(
        CategoryType type1, UUID userId1,
        CategoryType type2, UUID userId2
    );

    @Query("SELECT c FROM Category c WHERE c.isCustom = false")
    List<Category> findDefaultCategories();

    @Query("SELECT c FROM Category c WHERE c.user.id = :userId AND c.isCustom = true")
    List<Category> findUserCustomCategories(UUID userId);

    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId")
    List<Category> findSubcategories(UUID parentId);

    @Query("SELECT c FROM Category c WHERE (c.user.id = :userId OR c.user IS NULL) AND c.type = :type")
    List<Category> findAvailableCategories(UUID userId, CategoryType type);
}
