package com.pgs.inventorymanagement.service;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;

import org.springframework.stereotype.Service;

import com.opencsv.CSVWriter;
import com.pgs.inventorymanagement.model.Product;

@Service
public class CsvExportService {
    public String exportProducts(Iterable<Product> products) {
        StringWriter output = new StringWriter();
        try (CSVWriter csv = new CSVWriter(output)) {
            csv.writeNext(new String[] {"ID", "SKU", "Name", "Category", "Description",
                    "Quantity", "Unit Price", "Low Stock Threshold"});
            for (Product product : products) {
                csv.writeNext(new String[] {
                    product.getId(), product.getSku(), product.getName(), product.getCategory(),
                    product.getDescription(), String.valueOf(product.getQuantity()),
                    product.getUnitPrice().toPlainString(),
                    String.valueOf(product.getLowStockThreshold())
                });
            }
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to generate inventory CSV", ex);
        }
        return output.toString();
    }
}
