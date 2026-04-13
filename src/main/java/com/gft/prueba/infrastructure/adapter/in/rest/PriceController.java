package com.gft.prueba.infrastructure.adapter.in.rest;

import com.gft.prueba.application.port.in.GetApplicablePriceUseCase;
import com.gft.prueba.domain.model.Price;
import com.gft.prueba.infrastructure.adapter.in.rest.dto.PriceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/prices")
@Validated
@Tag(name = "Prices", description = "Consulta de precios")
public class PriceController {

    private final GetApplicablePriceUseCase getApplicablePriceUseCase;

    public PriceController(GetApplicablePriceUseCase getApplicablePriceUseCase) {
        this.getApplicablePriceUseCase = getApplicablePriceUseCase;
    }

    @GetMapping
    @Operation(summary = "Get applicable price")
    public ResponseEntity<PriceResponse> getPrice(
            @Parameter(description = "Fecha en formato ISO-8601")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime applicationDate,
            @Parameter(description = "Identificador del producto")
            @RequestParam @Positive Long productId,
            @Parameter(description = "Identificador de la marca")
            @RequestParam @Positive Long brandId) {

        Price price = getApplicablePriceUseCase.getApplicablePrice(applicationDate, productId, brandId);
        return ResponseEntity.ok(toResponse(price));
    }

    private PriceResponse toResponse(Price price) {
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
