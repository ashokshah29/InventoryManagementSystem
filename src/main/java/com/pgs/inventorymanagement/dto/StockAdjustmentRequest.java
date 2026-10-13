package com.pgs.inventorymanagement.dto;

import jakarta.validation.constraints.NotNull;

// Positive change = stock received; negative change = stock sold/removed.
public record StockAdjustmentRequest(@NotNull Integer change) { }
