-- Quy tắc Flyway: file migration đã chạy thì KHÔNG BAO GIỜ sửa.
-- Muốn thay đổi bảng thì tạo file mới V2__..., V3__...

CREATE TABLE flower (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sku         VARCHAR(30)    NOT NULL,
    name        VARCHAR(150)   NOT NULL,
    description VARCHAR(1000),
    category    VARCHAR(30)    NOT NULL,
    unit_price  NUMERIC(19, 4) NOT NULL,
    currency    VARCHAR(3)     NOT NULL,
    status      VARCHAR(20)    NOT NULL,
    version     BIGINT         NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ    NOT NULL,
    created_by  VARCHAR(100)   NOT NULL,
    updated_at  TIMESTAMPTZ,
    updated_by  VARCHAR(100),

    -- Ràng buộc ở tầng DB là chốt chặn cuối cùng, kể cả khi code có bug
    CONSTRAINT uk_flower_sku UNIQUE (sku),
    CONSTRAINT ck_flower_price_positive CHECK (unit_price > 0),
    CONSTRAINT ck_flower_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX idx_flower_status_created_at ON flower (status, created_at DESC);
