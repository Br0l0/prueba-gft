package com.gft.prueba.application.port.in;

import com.gft.prueba.domain.model.Price;

import java.time.LocalDateTime;

public interface GetApplicablePriceUseCase {
    Price getApplicablePrice(LocalDateTime applicationDate, Long productId, Long brandId);
}
