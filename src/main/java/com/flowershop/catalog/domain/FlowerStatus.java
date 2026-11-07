package com.flowershop.catalog.domain;

/** Không xóa cứng sản phẩm: đơn hàng cũ vẫn tham chiếu tới nó. Ngừng bán = INACTIVE. */
public enum FlowerStatus {
    ACTIVE,
    INACTIVE
}
