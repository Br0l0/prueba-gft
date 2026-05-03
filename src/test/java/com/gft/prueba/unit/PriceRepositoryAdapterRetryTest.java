package com.gft.prueba.unit;

import com.gft.prueba.application.port.out.LoadPricesPort;
import com.gft.prueba.infrastructure.adapter.out.persistence.PriceRepositoryAdapter;
import com.gft.prueba.infrastructure.adapter.out.persistence.SpringDataPriceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(PriceRepositoryAdapterRetryTest.RetryTestConfig.class)
class PriceRepositoryAdapterRetryTest {

    private final LoadPricesPort loadPricesPort;
    private final SpringDataPriceRepository repository;

    @Autowired
    PriceRepositoryAdapterRetryTest(LoadPricesPort loadPricesPort, SpringDataPriceRepository repository) {
        this.loadPricesPort = loadPricesPort;
        this.repository = repository;
    }

    @Test
    void retriesOnceWhenRepositoryFailsWithTransientError() {
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 10, 0);
        Long productId = 35455L;
        Long brandId = 1L;

        when(repository.findApplicablePrices(
                eq(brandId),
                eq(productId),
                eq(applicationDate),
                any(Pageable.class)
        ))
                .thenThrow(new TransientDataAccessResourceException("temporary failure"))
                .thenReturn(List.of());

        assertTrue(loadPricesPort.loadApplicablePrice(applicationDate, productId, brandId).isEmpty());

        verify(repository, times(2))
                .findApplicablePrices(eq(brandId), eq(productId), eq(applicationDate), any(Pageable.class));
    }

    @Configuration
    @EnableRetry
    static class RetryTestConfig {

        @Bean
        SpringDataPriceRepository springDataPriceRepository() {
            return mock(SpringDataPriceRepository.class);
        }

        @Bean
        LoadPricesPort loadPricesPort(SpringDataPriceRepository repository) {
            return new PriceRepositoryAdapter(repository);
        }
    }
}
