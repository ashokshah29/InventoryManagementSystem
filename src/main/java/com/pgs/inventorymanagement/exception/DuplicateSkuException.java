package com.pgs.inventorymanagement.exception;

public class DuplicateSkuException extends RuntimeException {
    public DuplicateSkuException(String sku) {
        super("Product SKU already exists: " + sku);
    }
}
