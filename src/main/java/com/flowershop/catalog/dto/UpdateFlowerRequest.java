package com.flowershop.catalog.dto;

import com.flowershop.catalog.domain.FlowerCategory;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Client phải gửi kèm version đã đọc được. Nếu trong lúc đó người khác đã sửa
 * (version trên DB đã tăng) thì request bị từ chối, tránh ghi đè mất dữ liệu.
 * SKU và tiền tệ không cho sửa sau khi tạo.
 */
public record UpdateFlowerRequest(
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

        @NotNull
        Long version
) {
}
