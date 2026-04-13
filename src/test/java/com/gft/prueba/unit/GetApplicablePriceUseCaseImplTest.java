package com.gft.prueba.unit;

import com.gft.prueba.application.port.out.LoadPricesPort;
import com.gft.prueba.application.usecase.GetApplicablePriceUseCaseImpl;
import com.gft.prueba.domain.exception.PriceNotFoundException;
import com.gft.prueba.domain.model.Price;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetApplicablePriceUseCaseImplTest {

    @Mock
    private LoadPricesPort loadPricesPort;

    @InjectMocks
    private GetApplicablePriceUseCaseImpl useCase;

    @Test
    void returnsTheApplicablePriceLoadedByThePort() {
        LocalDateTime date = LocalDateTime.of(2020, 6, 14, 10, 0);
        Long productId = 35455L;
        Long brandId = 1L;
        Price price = price(brandId, productId, 1, date.minusHours(1), date.plusHours(1), 0, "35.50");

        when(loadPricesPort.loadApplicablePrice(date, productId, brandId)).thenReturn(Optional.of(price));

        Price result = useCase.getApplicablePrice(date, productId, brandId);

        assertEquals(price, result);
    }

    @Test
    void throwsBusinessErrorWhenNoPriceExists() {
        LocalDateTime date = LocalDateTime.of(2020, 6, 15, 10, 0);
        Long productId = 35455L;
        Long brandId = 1L;

        when(loadPricesPort.loadApplicablePrice(date, productId, brandId)).thenReturn(Optional.empty());

        assertThrows(PriceNotFoundException.class, () -> useCase.getApplicablePrice(date, productId, brandId));
    }

    private Price price(Long brandId, Long productId, Integer priceList, LocalDateTime startDate,
                        LocalDateTime endDate, Integer priority, String amount) {
        return new Price(
                brandId,
                productId,
                priceList,
                startDate,
                endDate,
                priority,
                new BigDecimal(amount),
                "EUR"
        );
    }
}
