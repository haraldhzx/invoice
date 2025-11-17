package com.invoiceapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.invoiceapp.model.dto.CreateInvoiceRequest;
import com.invoiceapp.model.dto.RegisterRequest;
import com.invoiceapp.model.entity.Category;
import com.invoiceapp.model.entity.CategoryType;
import com.invoiceapp.repository.CategoryRepository;
import com.invoiceapp.repository.InvoiceRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class InvoiceControllerIntegrationTest {

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
    private InvoiceRepository invoiceRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private String authToken;
    private Category testCategory;

    @BeforeEach
    void setup() throws Exception {
        // Register and authenticate
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("invoiceuser@example.com");
        registerRequest.setPassword("SecurePass123!");

        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        authToken = objectMapper.readTree(response).get("accessToken").asText();

        // Get a default category from database
        testCategory = categoryRepository.findAll().stream()
                .filter(c -> c.getType() == CategoryType.EXPENSE)
                .findFirst()
                .orElseThrow();
    }

    @AfterEach
    void cleanup() {
        invoiceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createInvoice_WithValidRequest_ShouldReturn201() throws Exception {
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setVendorName("Test Vendor");
        request.setDate(LocalDate.now());
        request.setTotalAmount(new BigDecimal("99.99"));
        request.setCurrency("USD");
        request.setCategoryId(testCategory.getId());

        mockMvc.perform(post("/api/invoices")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vendorName", is("Test Vendor")))
                .andExpect(jsonPath("$.totalAmount", is(99.99)))
                .andExpect(jsonPath("$.currency", is("USD")))
                .andExpect(jsonPath("$.category.id", is(testCategory.getId().toString())));
    }

    @Test
    void createInvoice_WithoutAuthentication_ShouldReturn401() throws Exception {
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setVendorName("Test Vendor");
        request.setDate(LocalDate.now());
        request.setTotalAmount(new BigDecimal("99.99"));

        mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllInvoices_ShouldReturnPagedResult() throws Exception {
        // Create multiple invoices
        for (int i = 0; i < 3; i++) {
            CreateInvoiceRequest request = new CreateInvoiceRequest();
            request.setVendorName("Vendor " + i);
            request.setDate(LocalDate.now());
            request.setTotalAmount(new BigDecimal("50.00"));

            mockMvc.perform(post("/api/invoices")
                    .header("Authorization", "Bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        }

        // Get all invoices
        mockMvc.perform(get("/api/invoices")
                        .header("Authorization", "Bearer " + authToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.content[0].vendorName", containsString("Vendor")));
    }

    @Test
    void getInvoiceById_WhenExists_ShouldReturn200() throws Exception {
        // Create an invoice
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setVendorName("Specific Vendor");
        request.setDate(LocalDate.now());
        request.setTotalAmount(new BigDecimal("123.45"));

        String createResponse = mockMvc.perform(post("/api/invoices")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String invoiceId = objectMapper.readTree(createResponse).get("id").asText();

        // Get the invoice by ID
        mockMvc.perform(get("/api/invoices/" + invoiceId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(invoiceId)))
                .andExpect(jsonPath("$.vendorName", is("Specific Vendor")))
                .andExpect(jsonPath("$.totalAmount", is(123.45)));
    }

    @Test
    void updateInvoice_WithValidRequest_ShouldReturn200() throws Exception {
        // Create an invoice
        CreateInvoiceRequest createRequest = new CreateInvoiceRequest();
        createRequest.setVendorName("Original Vendor");
        createRequest.setDate(LocalDate.now());
        createRequest.setTotalAmount(new BigDecimal("100.00"));

        String createResponse = mockMvc.perform(post("/api/invoices")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String invoiceId = objectMapper.readTree(createResponse).get("id").asText();

        // Update the invoice
        String updateJson = "{\"vendorName\":\"Updated Vendor\",\"totalAmount\":200.00}";

        mockMvc.perform(put("/api/invoices/" + invoiceId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vendorName", is("Updated Vendor")))
                .andExpect(jsonPath("$.totalAmount", is(200.00)));
    }

    @Test
    void deleteInvoice_WhenExists_ShouldReturn204() throws Exception {
        // Create an invoice
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setVendorName("To Delete");
        request.setDate(LocalDate.now());
        request.setTotalAmount(new BigDecimal("50.00"));

        String createResponse = mockMvc.perform(post("/api/invoices")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String invoiceId = objectMapper.readTree(createResponse).get("id").asText();

        // Delete the invoice
        mockMvc.perform(delete("/api/invoices/" + invoiceId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        // Verify it's deleted
        mockMvc.perform(get("/api/invoices/" + invoiceId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTotalSpending_ShouldCalculateCorrectly() throws Exception {
        // Create multiple invoices
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();

        for (int i = 0; i < 3; i++) {
            CreateInvoiceRequest request = new CreateInvoiceRequest();
            request.setVendorName("Vendor " + i);
            request.setDate(LocalDate.now().minusDays(i));
            request.setTotalAmount(new BigDecimal("100.00"));

            mockMvc.perform(post("/api/invoices")
                    .header("Authorization", "Bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        }

        // Get total spending
        mockMvc.perform(get("/api/invoices/analytics/total-spending")
                        .header("Authorization", "Bearer " + authToken)
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(300.00)));
    }

    @Test
    void getSpendingByCategory_ShouldGroupCorrectly() throws Exception {
        // Create invoices in different categories
        Category category1 = testCategory;
        Category category2 = categoryRepository.findAll().stream()
                .filter(c -> c.getType() == CategoryType.EXPENSE && !c.getId().equals(category1.getId()))
                .findFirst()
                .orElseThrow();

        // Category 1: 2 invoices
        for (int i = 0; i < 2; i++) {
            CreateInvoiceRequest request = new CreateInvoiceRequest();
            request.setVendorName("Vendor " + i);
            request.setDate(LocalDate.now());
            request.setTotalAmount(new BigDecimal("100.00"));
            request.setCategoryId(category1.getId());

            mockMvc.perform(post("/api/invoices")
                    .header("Authorization", "Bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        }

        // Category 2: 1 invoice
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setVendorName("Other Vendor");
        request.setDate(LocalDate.now());
        request.setTotalAmount(new BigDecimal("50.00"));
        request.setCategoryId(category2.getId());

        mockMvc.perform(post("/api/invoices")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Get spending by category
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now();

        mockMvc.perform(get("/api/invoices/analytics/spending-by-category")
                        .header("Authorization", "Bearer " + authToken)
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$." + category1.getName(), is(200.00)))
                .andExpect(jsonPath("$." + category2.getName(), is(50.00)));
    }
}
