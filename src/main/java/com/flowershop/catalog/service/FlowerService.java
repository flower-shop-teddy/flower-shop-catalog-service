package com.flowershop.catalog.service;

import com.flowershop.catalog.domain.Flower;
import com.flowershop.catalog.domain.FlowerStatus;
import com.flowershop.catalog.dto.CreateFlowerRequest;
import com.flowershop.catalog.dto.FlowerResponse;
import com.flowershop.catalog.dto.UpdateFlowerRequest;
import com.flowershop.catalog.error.CatalogErrorCode;
import com.flowershop.catalog.mapper.FlowerMapper;
import com.flowershop.catalog.repository.FlowerRepository;
import com.flowershop.common.api.PageResponse;
import com.flowershop.common.error.BusinessException;
import com.flowershop.common.error.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlowerService {

    private static final int MAX_PAGE_SIZE = 100; // chặn client xin 1 triệu dòng một lần

    private final FlowerRepository repository;

    @Transactional
    public FlowerResponse create(CreateFlowerRequest req) {
        // Kiểm tra trước để trả lỗi đẹp...
        if (repository.existsBySku(req.sku())) {
            throw new BusinessException(CatalogErrorCode.SKU_ALREADY_EXISTS);
        }
        Flower flower = Flower.create(req.sku(), req.name(), req.description(),
                req.category(), req.unitPrice(), req.currency());
        try {
            Flower saved = repository.saveAndFlush(flower);
            log.info("Created flower id={} sku={}", saved.getId(), saved.getSku());
            return FlowerMapper.toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            // ...nhưng vẫn dựa vào UNIQUE constraint của DB làm chốt chặn cuối:
            // hai request cùng SKU đến cùng lúc đều có thể qua được bước kiểm tra ở trên.
            throw new BusinessException(CatalogErrorCode.SKU_ALREADY_EXISTS);
        }
    }

    @Transactional(readOnly = true)
    public FlowerResponse getById(Long id) {
        return FlowerMapper.toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<FlowerResponse> listActive(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.clamp(size, 1, MAX_PAGE_SIZE); // Math.clamp có từ Java 21
        Page<Flower> result = repository.findByStatus(FlowerStatus.ACTIVE,
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        return new PageResponse<>(
                result.getContent().stream().map(FlowerMapper::toResponse).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Transactional
    public FlowerResponse update(Long id, UpdateFlowerRequest req) {
        Flower flower = findOrThrow(id);
        if (!flower.isActive()) {
            throw new BusinessException(CatalogErrorCode.FLOWER_INACTIVE);
        }
        if (!Objects.equals(flower.getVersion(), req.version())) {
            throw new BusinessException(CommonErrorCode.CONCURRENT_MODIFICATION);
        }
        flower.updateDetails(req.name(), req.description(), req.category(), req.unitPrice());
        Flower saved = repository.saveAndFlush(flower); // flush để có version mới trả về client
        log.info("Updated flower id={} newVersion={}", saved.getId(), saved.getVersion());
        return FlowerMapper.toResponse(saved);
    }

    @Transactional
    public void deactivate(Long id) {
        Flower flower = findOrThrow(id);
        if (flower.isActive()) {
            flower.deactivate();
            log.info("Deactivated flower id={}", id);
        }
    }

    private Flower findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException(CatalogErrorCode.FLOWER_NOT_FOUND));
    }
}
