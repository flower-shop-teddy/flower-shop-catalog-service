package com.flowershop.catalog.error;

import com.flowershop.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

public enum CatalogErrorCode implements ErrorCode {
    FLOWER_NOT_FOUND("CAT-404-001", HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"),
    SKU_ALREADY_EXISTS("CAT-409-001", HttpStatus.CONFLICT, "Mã SKU đã tồn tại"),
    FLOWER_INACTIVE("CAT-422-001", HttpStatus.UNPROCESSABLE_ENTITY, "Sản phẩm đã ngừng kinh doanh");

    private final String code;
    private final HttpStatus status;
    private final String defaultMessage;

    CatalogErrorCode(String code, HttpStatus status, String defaultMessage) {
        this.code = code;
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    @Override public String code() { return code; }
    @Override public HttpStatus status() { return status; }
    @Override public String defaultMessage() { return defaultMessage; }
}
