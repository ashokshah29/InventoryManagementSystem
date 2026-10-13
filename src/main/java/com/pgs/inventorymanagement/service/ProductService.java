package com.pgs.inventorymanagement.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.pgs.inventorymanagement.dto.InventorySummary;
import com.pgs.inventorymanagement.dto.ProductRequest;
import com.pgs.inventorymanagement.exception.DuplicateSkuException;
import com.pgs.inventorymanagement.exception.ProductNotFoundException;
import com.pgs.inventorymanagement.exception.StockConflictException;
import com.pgs.inventorymanagement.model.Product;
import com.pgs.inventorymanagement.repository.ProductRepository;

@Service
public class ProductService {
    private final ProductRepository repository;
    private final MongoTemplate mongoTemplate;

    public ProductService(ProductRepository repository, MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
    }

    public List<Product> getAll() {
        return repository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public Product getById(String id) {
        return repository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    public Product create(ProductRequest request) {
        String sku = normalizeSku(request.sku());
        if (repository.existsBySku(sku)) {
            throw new DuplicateSkuException(sku);
        }

        Product product = new Product();
        copyFromRequest(product, request);
        Instant now = Instant.now();
        product.setCreatedAt(now);
        product.setUpdatedAt(now);
        return repository.save(product);
    }

    public Product update(String id, ProductRequest request) {
        Product product = getById(id);
        String normalizedSku = normalizeSku(request.sku());
        if (!Objects.equals(product.getSku(), normalizedSku) && repository.existsBySku(normalizedSku)) {
            throw new DuplicateSkuException(normalizedSku);
        }
        copyFromRequest(product, request);
        product.setUpdatedAt(Instant.now());
        return repository.save(product);
    }

    public void delete(String id) {
        Product product = getById(id);
        repository.delete(product);
    }

    public List<Product> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAll();
        }
        String query = keyword.trim();
        return repository.findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(query, query);
    }

    public List<Product> getByCategory(String category) {
        return repository.findByCategoryIgnoreCase(category.trim());
    }

    public List<Product> getLowStock() {
        return repository.findLowStockProducts();
    }

    // MongoDB findAndModify makes this stock increment/decrement atomic.
    // A conditional quantity check prevents negative inventory and integer overflow.
    public Product adjustStock(String id, Integer change) {
        if (change == null || change == 0) {
            throw new IllegalArgumentException("Stock change must be a non-zero integer.");
        }
        Query query = new Query(Criteria.where("_id").is(id));
        if (change < 0) {
            query.addCriteria(Criteria.where("quantity").gte(-(long) change));
        } else {
            query.addCriteria(Criteria.where("quantity").lte(Integer.MAX_VALUE - (long) change));
        }
        Update update = new Update().inc("quantity", change).set("updatedAt", Instant.now());
        Product updated = mongoTemplate.findAndModify(query, update,
                FindAndModifyOptions.options().returnNew(true), Product.class);
        if (updated == null) {
            if (!repository.existsById(id)) {
                throw new ProductNotFoundException(id);
            }
            throw new StockConflictException("Cannot adjust stock: insufficient quantity or quantity limit reached.");
        }
        return updated;
    }

    public InventorySummary summary() {
        List<Product> products = repository.findAll();
        long totalUnits = 0;
        long lowStockCount = 0;
        BigDecimal stockValue = BigDecimal.ZERO;
        for (Product product : products) {
            totalUnits += product.getQuantity();
            if (product.getQuantity() <= product.getLowStockThreshold()) {
                lowStockCount++;
            }
            stockValue = stockValue.add(product.getUnitPrice()
                .multiply(BigDecimal.valueOf(product.getQuantity())));
        }
        return new InventorySummary(products.size(), totalUnits, lowStockCount, stockValue);
    }

    private String normalizeSku(String sku) {
        return sku.trim().toUpperCase(Locale.ROOT);
    }

    private void copyFromRequest(Product product, ProductRequest request) {
        product.setSku(normalizeSku(request.sku()));
        product.setName(request.name().trim());
        product.setCategory(request.category().trim());
        product.setDescription(request.description() == null ? "" : request.description().trim());
        product.setQuantity(request.quantity());
        product.setUnitPrice(request.unitPrice());
        product.setLowStockThreshold(request.lowStockThreshold());
    }
}
