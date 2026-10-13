package com.pgs.inventorymanagement.exception;

public class StockConflictException extends RuntimeException {
    public StockConflictException(String message) {
        super(message);
    }
}
