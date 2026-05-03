package com.gft.prueba.integration;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "debug=false"
)
class PriceControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void usesBaseRateBeforeJune14PromotionStarts() {
        given()
                .queryParam("applicationDate", "2020-06-14T10:00:00")
                .queryParam("productId", "35455")
                .queryParam("brandId", "1")
        .when()
                .get("/api/v1/prices")
        .then()
                .statusCode(200)
                .body("productId", equalTo(35455))
                .body("brandId", equalTo(1))
                .body("priceList", equalTo(1))
                .body("finalPrice", equalTo(35.50F))
                .body("currency", equalTo("EUR"));
    }

    @Test
    void usesPromotionalRateDuringJune14Afternoon() {
        given()
                .queryParam("applicationDate", "2020-06-14T16:00:00")
                .queryParam("productId", "35455")
                .queryParam("brandId", "1")
        .when()
                .get("/api/v1/prices")
        .then()
                .statusCode(200)
                .body("priceList", equalTo(2))
                .body("finalPrice", equalTo(25.45F));
    }

    @Test
    void fallsBackToBaseRateAfterJune14PromotionEnds() {
        given()
                .queryParam("applicationDate", "2020-06-14T21:00:00")
                .queryParam("productId", "35455")
                .queryParam("brandId", "1")
        .when()
                .get("/api/v1/prices")
        .then()
                .statusCode(200)
                .body("priceList", equalTo(1))
                .body("finalPrice", equalTo(35.50F));
    }

    @Test
    void usesMorningRateOnJune15WhenItHasPriority() {
        given()
                .queryParam("applicationDate", "2020-06-15T10:00:00")
                .queryParam("productId", "35455")
                .queryParam("brandId", "1")
        .when()
                .get("/api/v1/prices")
        .then()
                .statusCode(200)
                .body("priceList", equalTo(3))
                .body("finalPrice", equalTo(30.50F));
    }

    @Test
    void usesFinalRateOnJune16Night() {
        given()
                .queryParam("applicationDate", "2020-06-16T21:00:00")
                .queryParam("productId", "35455")
                .queryParam("brandId", "1")
        .when()
                .get("/api/v1/prices")
        .then()
                .statusCode(200)
                .body("priceList", equalTo(4))
                .body("finalPrice", equalTo(38.95F));
    }

    @Test
    void returnsNotFoundForDateOutsideAnyPriceRange() {
        given()
                .queryParam("applicationDate", "2020-01-01T00:00:00")
                .queryParam("productId", "35455")
                .queryParam("brandId", "1")
        .when()
                .get("/api/v1/prices")
        .then()
                .statusCode(404)
                .body("code", equalTo("PRICE_NOT_FOUND"));
    }

    @Test
    void rejectsInvalidDateFormat() {
        given()
                .queryParam("applicationDate", "invalid-date")
                .queryParam("productId", "35455")
                .queryParam("brandId", "1")
        .when()
                .get("/api/v1/prices")
        .then()
                .statusCode(400)
                .body("code", equalTo("INVALID_REQUEST"));
    }

    @Test
    void rejectsInvalidIdentifiers() {
        given()
                .queryParam("applicationDate", "2020-06-14T10:00:00")
                .queryParam("productId", "0")
                .queryParam("brandId", "1")
        .when()
                .get("/api/v1/prices")
        .then()
                .statusCode(400)
                .body("code", equalTo("INVALID_REQUEST"));
    }

    @Test
    void rejectsMissingRequiredParameters() {
        given()
                .queryParam("productId", "35455")
                .queryParam("brandId", "1")
        .when()
                .get("/api/v1/prices")
        .then()
                .statusCode(400)
                .body("code", equalTo("INVALID_REQUEST"));
    }

    @Test
    void returnsCorrelationIdHeader() {
        given()
                .header("X-Correlation-Id", "test-correlation-id")
                .queryParam("applicationDate", "2020-06-14T10:00:00")
                .queryParam("productId", "35455")
                .queryParam("brandId", "1")
        .when()
                .get("/api/v1/prices")
        .then()
                .statusCode(200)
                .header("X-Correlation-Id", equalTo("test-correlation-id"));
    }

    @Test
    void replacesInvalidCorrelationIdHeader() {
        given()
                .header("X-Correlation-Id", "invalid header value")
                .queryParam("applicationDate", "2020-06-14T10:00:00")
                .queryParam("productId", "35455")
                .queryParam("brandId", "1")
        .when()
                .get("/api/v1/prices")
        .then()
                .statusCode(200)
                .header("X-Correlation-Id", not(equalTo("invalid header value")));
    }

    @Test
    void exposesHealthEndpointForOperationalMonitoring() {
        given()
        .when()
                .get("/actuator/health")
        .then()
                .statusCode(200)
                .body("status", notNullValue());
    }

    @Test
    void exposesMetricsEndpointForOperationalMonitoring() {
        given()
        .when()
                .get("/actuator/metrics")
        .then()
                .statusCode(200)
                .body("names", notNullValue());
    }

    @Test
    void exposesTheOpenApiContract() {
        given()
        .when()
                .get("/api-docs")
        .then()
                .statusCode(200)
                .body("info.title", equalTo("Pricing API"))
                .body("paths.'/api/v1/prices'.get.tags[0]", equalTo("Prices"));
    }
}
