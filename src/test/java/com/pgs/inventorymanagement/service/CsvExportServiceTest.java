package com.pgs.inventorymanagement.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import com.pgs.inventorymanagement.model.Product;

class CsvExportServiceTest {
    @Test
    void csvContainsHeaderAndProduct() {
        Product product = new Product();
        product.setId("1");
        product.setSku("BOOK-1");
        product.setName("Notebook");
        product.setCategory("Stationery");
        product.setDescription("");
        product.setQuantity(3);
        product.setUnitPrice(new BigDecimal("50.00"));
        product.setLowStockThreshold(1);

        String result = new CsvExportService().exportProducts(List.of(product));

        assertTrue(result.contains("SKU"));
        assertTrue(result.contains("BOOK-1"));
        assertTrue(result.contains("Notebook"));
    }
}
