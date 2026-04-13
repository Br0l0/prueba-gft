package com.gft.prueba.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Price(
    Long brandId,
    Long productId,
    Integer priceList,
    LocalDateTime startDate,
    LocalDateTime endDate,
    Integer priority,
    BigDecimal price,
    String currency
) {
}
