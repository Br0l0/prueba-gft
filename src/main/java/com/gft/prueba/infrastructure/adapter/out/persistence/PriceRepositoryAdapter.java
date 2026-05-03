package com.gft.prueba.infrastructure.adapter.out.persistence;

import com.gft.prueba.application.port.out.LoadPricesPort;
import com.gft.prueba.domain.model.Price;
import org.springframework.data.domain.PageRequest;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public class PriceRepositoryAdapter implements LoadPricesPort {

    private static final PageRequest FIRST_RESULT = PageRequest.of(0, 1);

    private final SpringDataPriceRepository repository;

    public PriceRepositoryAdapter(SpringDataPriceRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true, timeout = 2)
    @Retryable(
            retryFor = TransientDataAccessException.class,
            maxAttempts = 2,
            backoff = @Backoff(delay = 50)
    )
    public Optional<Price> loadApplicablePrice(LocalDateTime applicationDate, Long productId, Long brandId) {
        return repository
                .findApplicablePrices(
                        brandId,
                        productId,
                        applicationDate,
                        FIRST_RESULT
                )
                .stream()
                .findFirst()
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
