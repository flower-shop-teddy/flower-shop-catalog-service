package com.flowershop.catalog.mapper;

import com.flowershop.catalog.domain.Flower;
import com.flowershop.catalog.dto.FlowerResponse;

public final class FlowerMapper {

    private FlowerMapper() {
    }

    public static FlowerResponse toResponse(Flower f) {
        return new FlowerResponse(
                f.getId(), f.getSku(), f.getName(), f.getDescription(), f.getCategory(),
                f.getUnitPrice(), f.getCurrency(), f.getStatus(), f.getVersion(),
                f.getCreatedAt(), f.getUpdatedAt());
    }
}
