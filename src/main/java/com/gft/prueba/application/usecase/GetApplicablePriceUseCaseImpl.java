package com.gft.prueba.application.usecase;

import com.gft.prueba.application.port.in.GetApplicablePriceUseCase;
import com.gft.prueba.application.port.out.LoadPricesPort;
import com.gft.prueba.domain.exception.PriceNotFoundException;
import com.gft.prueba.domain.model.Price;

import java.time.LocalDateTime;

public class GetApplicablePriceUseCaseImpl implements GetApplicablePriceUseCase {

    private final LoadPricesPort loadPricesPort;

    public GetApplicablePriceUseCaseImpl(LoadPricesPort loadPricesPort) {
        this.loadPricesPort = loadPricesPort;
    }

    @Override
    public Price getApplicablePrice(LocalDateTime applicationDate, Long productId, Long brandId) {
        return loadPricesPort.loadApplicablePrice(applicationDate, productId, brandId)
                .orElseThrow(() -> new PriceNotFoundException(productId, brandId, applicationDate));
    }
}
