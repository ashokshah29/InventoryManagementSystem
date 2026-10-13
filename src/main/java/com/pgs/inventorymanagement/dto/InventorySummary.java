package com.pgs.inventorymanagement.dto;

import java.math.BigDecimal;

public record InventorySummary(
    long totalProducts,
    long totalUnits,
    long lowStockProducts,
    BigDecimal totalStockValue
) { }
