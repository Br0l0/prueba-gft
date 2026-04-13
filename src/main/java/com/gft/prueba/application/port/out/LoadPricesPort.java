package com.gft.prueba.application.port.out;

import com.gft.prueba.domain.model.Price;

import java.time.LocalDateTime;
import java.util.Optional;

public interface LoadPricesPort {
    Optional<Price> loadApplicablePrice(LocalDateTime applicationDate, Long productId, Long brandId);
}
