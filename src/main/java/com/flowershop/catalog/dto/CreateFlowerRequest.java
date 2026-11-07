package com.flowershop.catalog.dto;

import com.flowershop.catalog.domain.FlowerCategory;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateFlowerRequest(
        @NotBlank
        @Pattern(regexp = "^[A-Z0-9-]{3,30}$", message = "SKU chỉ gồm chữ IN HOA, số, dấu '-', dài 3-30 ký tự")
        String sku,

        @NotBlank @Size(max = 150)
        String name,

        @Size(max = 1000)
        String description,

        @NotNull
        FlowerCategory category,

        @NotNull
        @DecimalMin(value = "0", inclusive = false, message = "Giá phải lớn hơn 0")
        @Digits(integer = 15, fraction = 4)
        BigDecimal unitPrice,

        @NotBlank
        @Pattern(regexp = "^[A-Z]{3}$", message = "Mã tiền tệ theo ISO 4217, ví dụ VND")
        String currency
) {
}
