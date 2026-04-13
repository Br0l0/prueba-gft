package com.gft.prueba.infrastructure.adapter.out.persistence;

import com.gft.prueba.application.port.out.LoadPricesPort;
import com.gft.prueba.domain.model.Price;

import java.time.LocalDateTime;
import java.util.Optional;

public class PriceRepositoryAdapter implements LoadPricesPort {

    private final SpringDataPriceRepository repository;

    public PriceRepositoryAdapter(SpringDataPriceRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Price> loadApplicablePrice(LocalDateTime applicationDate, Long productId, Long brandId) {
        return repository
                .findFirstByBrandIdAndProductIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByPriorityDescStartDateDesc(
                        brandId,
                        productId,
                        applicationDate,
                        applicationDate
                )
                .map(this::toDomain);
    }

    private Price toDomain(PriceEntity entity) {
        return new Price(
                entity.getBrandId(),
                entity.getProductId(),
                entity.getPriceList(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getPriority(),
                entity.getPrice(),
                entity.getCurrency()
        );
    }
}
