package com.pgs.inventorymanagement.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductRequest(
    @NotBlank @Size(max = 40) String sku,
    @NotBlank @Size(max = 120) String name,
    @NotBlank @Size(max = 80) String category,
    @Size(max = 500) String description,
    @NotNull @Min(0) Integer quantity,
    @NotNull @DecimalMin("0.00") @Digits(integer = 12, fraction = 2) BigDecimal unitPrice,
    @NotNull @Min(0) Integer lowStockThreshold
) { }
