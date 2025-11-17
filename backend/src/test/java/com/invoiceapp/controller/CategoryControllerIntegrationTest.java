package com.invoiceapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.invoiceapp.model.dto.CategoryDto;
import com.invoiceapp.model.dto.RegisterRequest;
import com.invoiceapp.model.entity.CategoryType;
import com.invoiceapp.repository.CategoryRepository;
import com.invoiceapp.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class CategoryControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> true);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private String authToken;

    @BeforeEach
    void setup() throws Exception {
        // Register and authenticate
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("categoryuser@example.com");
        registerRequest.setPassword("SecurePass123!");

        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        authToken = objectMapper.readTree(response).get("accessToken").asText();
    }

    @AfterEach
    void cleanup() {
        // Only delete custom categories (don't delete defaults)
        categoryRepository.findAll().stream()
                .filter(c -> c.isCustom())
                .forEach(categoryRepository::delete);
        userRepository.deleteAll();
    }

    @Test
    void getAllCategories_ShouldReturnDefaultCategories() throws Exception {
        mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].name", notNullValue()))
                .andExpect(jsonPath("$[0].type", notNullValue()));
    }

    @Test
    void getAllCategories_WithoutAuth_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCategoriesByType_Expense_ShouldReturnOnlyExpenseCategories() throws Exception {
        mockMvc.perform(get("/api/categories/type/EXPENSE")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].type", everyItem(is("EXPENSE"))));
    }

    @Test
    void getCategoriesByType_Income_ShouldReturnOnlyIncomeCategories() throws Exception {
        mockMvc.perform(get("/api/categories/type/INCOME")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].type", everyItem(is("INCOME"))));
    }

    @Test
    void createCategory_WithValidRequest_ShouldReturn201() throws Exception {
        CategoryDto request = new CategoryDto();
        request.setName("Custom Category");
        request.setType(CategoryType.EXPENSE);
        request.setIcon("custom_icon");
        request.setColor("#FF5733");
        request.setDescription("My custom category");

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Custom Category")))
                .andExpect(jsonPath("$.type", is("EXPENSE")))
                .andExpect(jsonPath("$.icon", is("custom_icon")))
                .andExpect(jsonPath("$.color", is("#FF5733")))
                .andExpect(jsonPath("$.custom", is(true)));
    }

    @Test
    void createCategory_WithoutName_ShouldReturn400() throws Exception {
        CategoryDto request = new CategoryDto();
        request.setType(CategoryType.EXPENSE);

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCategoryById_WhenExists_ShouldReturn200() throws Exception {
        // Get first default category
        String categoriesResponse = mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String categoryId = objectMapper.readTree(categoriesResponse).get(0).get("id").asText();

        mockMvc.perform(get("/api/categories/" + categoryId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(categoryId)));
    }

    @Test
    void updateCategory_CustomCategory_ShouldReturn200() throws Exception {
        // Create a custom category first
        CategoryDto createRequest = new CategoryDto();
        createRequest.setName("Original Name");
        createRequest.setType(CategoryType.EXPENSE);

        String createResponse = mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String categoryId = objectMapper.readTree(createResponse).get("id").asText();

        // Update the category
        CategoryDto updateRequest = new CategoryDto();
        updateRequest.setName("Updated Name");
        updateRequest.setIcon("new_icon");

        mockMvc.perform(put("/api/categories/" + categoryId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Name")))
                .andExpect(jsonPath("$.icon", is("new_icon")));
    }

    @Test
    void deleteCategory_CustomCategory_ShouldReturn204() throws Exception {
        // Create a custom category first
        CategoryDto createRequest = new CategoryDto();
        createRequest.setName("To Delete");
        createRequest.setType(CategoryType.EXPENSE);

        String createResponse = mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String categoryId = objectMapper.readTree(createResponse).get("id").asText();

        // Delete the category
        mockMvc.perform(delete("/api/categories/" + categoryId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        // Verify it's deleted
        mockMvc.perform(get("/api/categories/" + categoryId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCategory_DefaultCategory_ShouldReturn403() throws Exception {
        // Get a default category
        String categoriesResponse = mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String defaultCategoryId = objectMapper.readTree(categoriesResponse)
                .elements().next().get("id").asText();

        // Try to delete default category (should fail)
        mockMvc.perform(delete("/api/categories/" + defaultCategoryId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void createSubcategory_WithParentId_ShouldReturn201() throws Exception {
        // Get a parent category
        String categoriesResponse = mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String parentId = objectMapper.readTree(categoriesResponse).get(0).get("id").asText();

        // Create subcategory
        CategoryDto request = new CategoryDto();
        request.setName("Subcategory");
        request.setType(CategoryType.EXPENSE);
        request.setParentId(java.util.UUID.fromString(parentId));

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Subcategory")))
                .andExpect(jsonPath("$.parentId", is(parentId)));
    }

    @Test
    void getSubcategories_ShouldReturnChildCategories() throws Exception {
        // Create a parent category
        CategoryDto parentRequest = new CategoryDto();
        parentRequest.setName("Parent Category");
        parentRequest.setType(CategoryType.EXPENSE);

        String parentResponse = mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(parentRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String parentId = objectMapper.readTree(parentResponse).get("id").asText();

        // Create subcategories
        for (int i = 0; i < 3; i++) {
            CategoryDto childRequest = new CategoryDto();
            childRequest.setName("Child " + i);
            childRequest.setType(CategoryType.EXPENSE);
            childRequest.setParentId(java.util.UUID.fromString(parentId));

            mockMvc.perform(post("/api/categories")
                    .header("Authorization", "Bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(childRequest)))
                    .andExpect(status().isCreated());
        }

        // Get subcategories
        mockMvc.perform(get("/api/categories/" + parentId + "/subcategories")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].parentId", everyItem(is(parentId))));
    }
}
