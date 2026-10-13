package com.pgs.inventorymanagement.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import com.pgs.inventorymanagement.model.Product;

public interface ProductRepository extends MongoRepository<Product, String> {
    boolean existsBySku(String sku);
    List<Product> findByCategoryIgnoreCase(String category);
    List<Product> findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(String name, String sku);

    @Query("{ '$expr': { '$lte': ['$quantity', '$lowStockThreshold'] } }")
    List<Product> findLowStockProducts();
}
