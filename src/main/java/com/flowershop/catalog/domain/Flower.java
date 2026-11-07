package com.flowershop.catalog.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entity không có setter tùy tiện: muốn đổi trạng thái phải đi qua method nghiệp vụ
 * (updateDetails, deactivate) để luật nghiệp vụ nằm một chỗ.
 */
@Entity
@Table(name = "flower")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA cần, code ngoài không gọi được
public class Flower {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30, updatable = false)
    private String sku;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING) // lưu chữ, không lưu số thứ tự (thêm enum mới sẽ lệch dữ liệu)
    @Column(nullable = false, length = 30)
    private FlowerCategory category;

    // Tiền: BigDecimal + NUMERIC(19,4). TUYỆT ĐỐI không dùng double/float.
    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(nullable = false, length = 3)
    private String currency; // mã ISO 4217: VND, USD...

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FlowerStatus status;

    // Optimistic lock: hai người cùng sửa, người lưu sau sẽ bị từ chối thay vì ghi đè im lặng
    @Version
    private Long version;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false, length = 100)
    private String createdBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public static Flower create(String sku, String name, String description,
                                FlowerCategory category, BigDecimal unitPrice, String currency) {
        Flower f = new Flower();
        f.sku = sku;
        f.name = name;
        f.description = description;
        f.category = category;
        f.unitPrice = unitPrice;
        f.currency = currency;
        f.status = FlowerStatus.ACTIVE;
        return f;
    }

    public void updateDetails(String name, String description, FlowerCategory category, BigDecimal unitPrice) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.unitPrice = unitPrice;
    }

    public void deactivate() {
        this.status = FlowerStatus.INACTIVE;
    }

    public boolean isActive() {
        return status == FlowerStatus.ACTIVE;
    }
}
