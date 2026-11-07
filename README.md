# flower-shop-catalog-service

Danh mục hoa: CRUD hoa, giá (BigDecimal + mã tiền tệ), soft delete, optimistic lock. PostgreSQL + Flyway.

Cổng local: `8081`

## Chạy

Cách khuyên dùng: chạy cả hệ thống từ repo `flower-shop-workspace` (`./clone-all.sh` rồi `./run-local.sh`).

Build riêng repo này:

```bash
mvn verify
```

Cần parent `com.flowershop:flower-shop-parent` từ repo `flower-shop-platform`: nằm sẵn ở `../platform` trong workspace,
hoặc đã `mvn install` vào `~/.m2`.
