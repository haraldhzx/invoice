package com.invoiceapp.service;

import com.invoiceapp.model.dto.CreateInvoiceRequest;
import com.invoiceapp.model.dto.InvoiceDto;
import com.invoiceapp.model.dto.UpdateInvoiceRequest;
import com.invoiceapp.model.entity.Category;
import com.invoiceapp.model.entity.Invoice;
import com.invoiceapp.model.entity.InvoiceStatus;
import com.invoiceapp.model.entity.User;
import com.invoiceapp.repository.CategoryRepository;
import com.invoiceapp.repository.InvoiceRepository;
import com.invoiceapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private InvoiceService invoiceService;

    private User testUser;
    private Category testCategory;
    private Invoice testInvoice;
    private CreateInvoiceRequest createRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");

        testCategory = new Category();
        testCategory.setId(UUID.randomUUID());
        testCategory.setName("Food & Dining");

        testInvoice = new Invoice();
        testInvoice.setId(UUID.randomUUID());
        testInvoice.setUser(testUser);
        testInvoice.setVendorName("Test Vendor");
        testInvoice.setDate(LocalDate.now());
        testInvoice.setTotalAmount(new BigDecimal("100.50"));
        testInvoice.setCurrency("USD");
        testInvoice.setStatus(InvoiceStatus.COMPLETED);
        testInvoice.setCategory(testCategory);

        createRequest = new CreateInvoiceRequest();
        createRequest.setVendorName("New Vendor");
        createRequest.setDate(LocalDate.now());
        createRequest.setTotalAmount(new BigDecimal("50.00"));
        createRequest.setCurrency("USD");
    }

    @Test
    void getAllInvoices_ShouldReturnPagedInvoices() {
        // Arrange
        List<Invoice> invoices = Arrays.asList(testInvoice);
        Page<Invoice> page = new PageImpl<>(invoices);
        Pageable pageable = PageRequest.of(0, 20);

        when(invoiceRepository.findByUserId(testUser.getId(), pageable)).thenReturn(page);

        // Act
        Page<InvoiceDto> result = invoiceService.getAllInvoices(testUser.getId(), null, null, null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Vendor", result.getContent().get(0).getVendorName());
        verify(invoiceRepository).findByUserId(testUser.getId(), pageable);
    }

    @Test
    void getInvoiceById_WhenExists_ShouldReturnInvoice() {
        // Arrange
        UUID invoiceId = testInvoice.getId();
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(testInvoice));

        // Act
        InvoiceDto result = invoiceService.getInvoiceById(invoiceId);

        // Assert
        assertNotNull(result);
        assertEquals(invoiceId, result.getId());
        assertEquals("Test Vendor", result.getVendorName());
        verify(invoiceRepository).findById(invoiceId);
    }

    @Test
    void getInvoiceById_WhenNotExists_ShouldThrowException() {
        // Arrange
        UUID invoiceId = UUID.randomUUID();
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> invoiceService.getInvoiceById(invoiceId));
    }

    @Test
    void createInvoice_WithValidRequest_ShouldCreateAndReturnInvoice() {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);

        // Act
        InvoiceDto result = invoiceService.createInvoice(createRequest, testUser.getId());

        // Assert
        assertNotNull(result);
        assertEquals("Test Vendor", result.getVendorName());
        verify(userRepository).findById(testUser.getId());
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void createInvoice_WithCategory_ShouldAssignCategory() {
        // Arrange
        UUID categoryId = testCategory.getId();
        createRequest.setCategoryId(categoryId);

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);

        // Act
        InvoiceDto result = invoiceService.createInvoice(createRequest, testUser.getId());

        // Assert
        assertNotNull(result);
        assertNotNull(result.getCategory());
        assertEquals("Food & Dining", result.getCategory().getName());
        verify(categoryRepository).findById(categoryId);
    }

    @Test
    void updateInvoice_WithValidRequest_ShouldUpdateAndReturnInvoice() {
        // Arrange
        UUID invoiceId = testInvoice.getId();
        UpdateInvoiceRequest updateRequest = new UpdateInvoiceRequest();
        updateRequest.setVendorName("Updated Vendor");
        updateRequest.setStatus(InvoiceStatus.REVIEW_REQUIRED);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(testInvoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);

        // Act
        InvoiceDto result = invoiceService.updateInvoice(invoiceId, updateRequest);

        // Assert
        assertNotNull(result);
        verify(invoiceRepository).save(testInvoice);
    }

    @Test
    void deleteInvoice_WhenExists_ShouldDeleteInvoice() {
        // Arrange
        UUID invoiceId = testInvoice.getId();
        when(invoiceRepository.existsById(invoiceId)).thenReturn(true);

        // Act
        invoiceService.deleteInvoice(invoiceId);

        // Assert
        verify(invoiceRepository).deleteById(invoiceId);
    }

    @Test
    void deleteInvoice_WhenNotExists_ShouldThrowException() {
        // Arrange
        UUID invoiceId = UUID.randomUUID();
        when(invoiceRepository.existsById(invoiceId)).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> invoiceService.deleteInvoice(invoiceId));
        verify(invoiceRepository, never()).deleteById(any());
    }

    @Test
    void getTotalSpending_ShouldReturnTotalAmount() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusMonths(1);
        LocalDate endDate = LocalDate.now();
        BigDecimal expectedTotal = new BigDecimal("500.00");

        when(invoiceRepository.getTotalAmountByUserAndDateRange(testUser.getId(), startDate, endDate))
                .thenReturn(expectedTotal);

        // Act
        BigDecimal result = invoiceService.getTotalSpending(testUser.getId(), startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(expectedTotal, result);
        verify(invoiceRepository).getTotalAmountByUserAndDateRange(testUser.getId(), startDate, endDate);
    }

    @Test
    void getSpendingByCategory_ShouldReturnCategoryTotals() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusMonths(1);
        LocalDate endDate = LocalDate.now();

        Object[] row1 = {testCategory.getName(), new BigDecimal("200.00")};
        Object[] row2 = {"Transportation", new BigDecimal("150.00")};
        List<Object[]> mockResults = Arrays.asList(row1, row2);

        when(invoiceRepository.getSpendingByCategory(testUser.getId(), startDate, endDate))
                .thenReturn(mockResults);

        // Act
        var result = invoiceService.getSpendingByCategory(testUser.getId(), startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(new BigDecimal("200.00"), result.get("Food & Dining"));
        assertEquals(new BigDecimal("150.00"), result.get("Transportation"));
    }
}
