package com.gft.prueba.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SpringDataPriceRepository extends JpaRepository<PriceEntity, Long> {

    @Query("""
            SELECT price
            FROM PriceEntity price
            WHERE price.brandId = :brandId
              AND price.productId = :productId
              AND price.startDate <= :applicationDate
              AND price.endDate >= :applicationDate
            ORDER BY price.priority DESC, price.startDate DESC
            """)
    List<PriceEntity> findApplicablePrices(
            @Param("brandId") Long brandId,
            @Param("productId") Long productId,
            @Param("applicationDate") LocalDateTime applicationDate,
            Pageable pageable
    );
}
