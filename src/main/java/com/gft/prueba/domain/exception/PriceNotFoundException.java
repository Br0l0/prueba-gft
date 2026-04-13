package com.gft.prueba.domain.exception;

import java.time.LocalDateTime;

public class PriceNotFoundException extends RuntimeException {
    public PriceNotFoundException(Long productId, Long brandId, LocalDateTime applicationDate) {
        super("No applicable price found for productId=" + productId + ", brandId=" + brandId + " and applicationDate=" + applicationDate);
    }
}