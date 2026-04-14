package com.gft.prueba.infrastructure.adapter.in.rest.mapper;

import com.gft.prueba.domain.model.Price;
import com.gft.prueba.infrastructure.adapter.in.rest.dto.PriceResponse;
import org.springframework.stereotype.Component;

@Component
public class PriceResponseMapper {

    public PriceResponse toResponse(Price price) {
        return new PriceResponse(
                price.productId(),
                price.brandId(),
                price.priceList(),
                price.startDate(),
                price.endDate(),
                price.price(),
                price.currency()
        );
    }
}
