package com.flowershop.catalog.web;

import com.flowershop.catalog.dto.CreateFlowerRequest;
import com.flowershop.catalog.dto.FlowerResponse;
import com.flowershop.catalog.dto.UpdateFlowerRequest;
import com.flowershop.catalog.service.FlowerService;
import com.flowershop.common.api.ApiResponse;
import com.flowershop.common.api.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** API có version (/v1): sau này đổi format thì làm /v2, client cũ vẫn chạy. */
@RestController
@RequestMapping("/api/v1/flowers")
@RequiredArgsConstructor
public class FlowerController {

    private final FlowerService service;

    @PostMapping
    public ResponseEntity<ApiResponse<FlowerResponse>> create(@Valid @RequestBody CreateFlowerRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(req)));
    }

    @GetMapping("/{id}")
    public ApiResponse<FlowerResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @GetMapping
    public ApiResponse<PageResponse<FlowerResponse>> list(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(service.listActive(page, size));
    }

    @PutMapping("/{id}")
    public ApiResponse<FlowerResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateFlowerRequest req) {
        return ApiResponse.success(service.update(id, req));
    }

    /** "Xóa" = ngừng kinh doanh (soft delete). Dữ liệu vẫn còn để đối chiếu đơn hàng cũ. */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deactivate(@PathVariable Long id) {
        service.deactivate(id);
        return ApiResponse.success("Đã ngừng kinh doanh sản phẩm", null);
    }
}
