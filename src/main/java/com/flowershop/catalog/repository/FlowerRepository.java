package com.flowershop.catalog.repository;

import com.flowershop.catalog.domain.Flower;
import com.flowershop.catalog.domain.FlowerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlowerRepository extends JpaRepository<Flower, Long> {

    boolean existsBySku(String sku);

    Page<Flower> findByStatus(FlowerStatus status, Pageable pageable);
}
