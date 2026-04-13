package com.gft.prueba.integration;

import com.gft.prueba.domain.model.Price;
import com.gft.prueba.infrastructure.adapter.out.persistence.PriceRepositoryAdapter;
import com.gft.prueba.infrastructure.adapter.out.persistence.SpringDataPriceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(showSql = false, properties = "debug=false")
class PriceRepositoryAdapterIntegrationTest {

    @Autowired
    private SpringDataPriceRepository repository;

    @Test
    @Sql(statements = {
            "INSERT INTO prices (brand_id, start_date, end_date, price_list, product_id, priority, price, curr) " +
                    "VALUES (9, '2020-06-14T00:00:00', '2020-06-20T23:59:59', 10, 99999, 5, 19.99, 'EUR')",
            "INSERT INTO prices (brand_id, start_date, end_date, price_list, product_id, priority, price, curr) " +
                    "VALUES (9, '2020-06-15T00:00:00', '2020-06-20T23:59:59', 11, 99999, 5, 17.99, 'EUR')"
    })
    void returnsMostRecentStartDateWhenPriorityIsTied() {
        PriceRepositoryAdapter adapter = new PriceRepositoryAdapter(repository);
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 16, 10, 0);

        Optional<Price> result = adapter.loadApplicablePrice(applicationDate, 99999L, 9L);

        assertTrue(result.isPresent());
        assertEquals(11, result.get().priceList());
        assertEquals(LocalDateTime.of(2020, 6, 15, 0, 0), result.get().startDate());
    }
}
