package com.pgs.inventorymanagement.controller;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.pgs.inventorymanagement.dto.InventorySummary;
import com.pgs.inventorymanagement.dto.ProductRequest;
import com.pgs.inventorymanagement.dto.StockAdjustmentRequest;
import com.pgs.inventorymanagement.model.Product;
import com.pgs.inventorymanagement.service.CsvExportService;
import com.pgs.inventorymanagement.service.ProductService;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService service;
    private final CsvExportService csvExportService;

    public ProductController(ProductService service, CsvExportService csvExportService) {
        this.service = service;
        this.csvExportService = csvExportService;
    }

    @PostMapping
    public ResponseEntity<Product> create(@Valid @RequestBody ProductRequest request,
                                          UriComponentsBuilder uriBuilder) {
        Product created = service.create(request);
        URI location = uriBuilder.path("/api/products/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public List<Product> all() {
        return service.getAll();
    }

    @GetMapping("/search")
    public List<Product> search(@RequestParam(required = false) String keyword) {
        return service.search(keyword);
    }

    @GetMapping("/category/{category}")
    public List<Product> category(@PathVariable String category) {
        return service.getByCategory(category);
    }

    @GetMapping("/low-stock")
    public List<Product> lowStock() {
        return service.getLowStock();
    }

    @GetMapping("/summary")
    public InventorySummary summary() {
        return service.summary();
    }

    @GetMapping(value = "/export/csv", produces = "text/csv")
    public ResponseEntity<String> exportCsv() {
        String csv = csvExportService.exportProducts(service.getAll());
        return ResponseEntity.ok()
            .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inventory.csv")
            .body(csv);
    }

    @GetMapping("/{id}")
    public Product one(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public Product update(@PathVariable String id, @Valid @RequestBody ProductRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/stock")
    public Product adjustStock(@PathVariable String id, @Valid @RequestBody StockAdjustmentRequest request) {
        return service.adjustStock(id, request.change());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
