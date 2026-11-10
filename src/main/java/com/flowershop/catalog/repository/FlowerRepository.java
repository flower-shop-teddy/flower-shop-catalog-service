package com.flowershop.catalog.repository;

import com.flowershop.catalog.domain.Flower;
import com.flowershop.catalog.domain.FlowerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FlowerRepository extends JpaRepository<Flower, Long> {

    boolean existsBySku(String sku);

    Optional<Flower> findBySku(String sku);

    Page<Flower> findByStatus(FlowerStatus status, Pageable pageable);
}
