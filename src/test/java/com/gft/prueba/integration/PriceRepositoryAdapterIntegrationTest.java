package com.gft.prueba.integration;

import com.gft.prueba.domain.model.Price;
import com.gft.prueba.infrastructure.adapter.out.persistence.PriceRepositoryAdapter;
import com.gft.prueba.infrastructure.adapter.out.persistence.SpringDataPriceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(showSql = false, properties = "debug=false")
class PriceRepositoryAdapterIntegrationTest {

    @Autowired
    private SpringDataPriceRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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

    @Test
    void createsLookupIndexForApplicablePriceQueries() {
        Integer indexCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM INFORMATION_SCHEMA.INDEXES
                WHERE INDEX_NAME = 'IDX_PRICES_LOOKUP'
                """, Integer.class);

        assertEquals(1, indexCount);
    }

    @Test
    void keepsLookupCorrectWithVolumeData() {
        insertVolumePrices(2_000);
        PriceRepositoryAdapter adapter = new PriceRepositoryAdapter(repository);
        LocalDateTime applicationDate = LocalDateTime.of(2020, 7, 15, 10, 0);

        Optional<Price> result = adapter.loadApplicablePrice(applicationDate, 77777L, 7L);

        assertTrue(result.isPresent());
        assertEquals(9001, result.get().priceList());
        assertEquals(new BigDecimal("29.99"), result.get().price());
    }

    private void insertVolumePrices(int rows) {
        jdbcTemplate.batchUpdate("""
                INSERT INTO prices (brand_id, start_date, end_date, price_list, product_id, priority, price, curr)
                VALUES (?, ?, ?, ?, ?, ?, ?, 'EUR')
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, 7L);
                ps.setObject(2, LocalDateTime.of(2020, 1, 1, 0, 0).plusDays(i % 180));
                ps.setObject(3, LocalDateTime.of(2020, 12, 31, 23, 59));
                ps.setInt(4, i + 1);
                ps.setLong(5, i % 2 == 0 ? 77777L : 88888L);
                ps.setInt(6, 0);
                ps.setBigDecimal(7, new BigDecimal("19.99"));
            }

            @Override
            public int getBatchSize() {
                return rows;
            }
        });

        jdbcTemplate.update("""
                INSERT INTO prices (brand_id, start_date, end_date, price_list, product_id, priority, price, curr)
                VALUES (7, '2020-07-01T00:00:00', '2020-07-31T23:59:59', 9001, 77777, 5, 29.99, 'EUR')
                """);
    }
}
