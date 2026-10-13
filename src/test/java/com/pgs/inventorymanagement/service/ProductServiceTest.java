package com.pgs.inventorymanagement.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.pgs.inventorymanagement.dto.ProductRequest;
import com.pgs.inventorymanagement.exception.DuplicateSkuException;
import com.pgs.inventorymanagement.exception.ProductNotFoundException;
import com.pgs.inventorymanagement.model.Product;
import com.pgs.inventorymanagement.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock ProductRepository repository;
    @Mock MongoTemplate mongoTemplate;
    @InjectMocks ProductService service;
    private Product existing;

    @BeforeEach
    void setup() {
        existing = new Product();
        existing.setId("product123");
        existing.setSku("PEN-001");
        existing.setName("Blue Pen");
        existing.setCategory("Stationery");
        existing.setQuantity(12);
        existing.setUnitPrice(new BigDecimal("15.00"));
        existing.setLowStockThreshold(5);
    }

    @Test
    void createNormalizesSku() {
        var request = new ProductRequest("pen-002", " Black Pen ", "Stationery", "", 10,
            new BigDecimal("20.00"), 4);
        when(repository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product saved = service.create(request);

        assertEquals("PEN-002", saved.getSku());
        assertEquals("Black Pen", saved.getName());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void duplicateSkuIsRejected() {
        when(repository.existsBySku("PEN-001")).thenReturn(true);
        var request = new ProductRequest("pen-001", "Blue Pen", "Stationery", "", 10,
            new BigDecimal("15.00"), 5);
        assertThrows(DuplicateSkuException.class, () -> service.create(request));
        verify(repository, never()).save(any(Product.class));
    }

    @Test
    void getMissingProductReturnsNotFoundError() {
        when(repository.findById("missing")).thenReturn(Optional.empty());
        assertThrows(ProductNotFoundException.class, () -> service.getById("missing"));
    }

    @Test
    void summaryCalculatesTotalValueAndLowStock() {
        when(repository.findAll()).thenReturn(List.of(existing));
        var result = service.summary();
        assertEquals(1, result.totalProducts());
        assertEquals(12, result.totalUnits());
        assertEquals(0, result.lowStockProducts());
        assertEquals(0, new BigDecimal("180.00").compareTo(result.totalStockValue()));
    }

    @Test
    void zeroStockAdjustmentIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.adjustStock("product123", 0));
        verifyNoInteractions(mongoTemplate);
    }
}
