package com.flowershop.catalog.dto;

import com.flowershop.catalog.domain.FlowerCategory;
import com.flowershop.catalog.domain.FlowerStatus;

import java.math.BigDecimal;
import java.time.Instant;

/** Không bao giờ trả thẳng Entity ra API: lộ cấu trúc DB và dễ dính lỗi lazy loading. */
public record FlowerResponse(
        Long id,
        String sku,
        String name,
        String description,
        FlowerCategory category,
        BigDecimal unitPrice,
        String currency,
        FlowerStatus status,
        Long version,
        Instant createdAt,
        Instant updatedAt
) {
}
