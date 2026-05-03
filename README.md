# Prueba técnica - Pricing API

Este proyecto resuelve la prueba de consulta de precios para un producto, una marca y una fecha concreta.

La idea es sencilla: dada una fecha de aplicación, un `productId` y un `brandId`, la API devuelve la tarifa que corresponde según los rangos de fechas y la prioridad definida en la tabla `prices`.

## Tecnologías utilizadas

- Java 21
- Spring Boot 3.3.0
- Maven
- Spring Web
- Spring Validation
- Spring Data JPA
- Spring Boot Actuator
- H2 en memoria
- springdoc-openapi
- Spring Retry
- JUnit 5
- Mockito
- RestAssured

## Arquitectura

He usado arquitectura hexagonal separando el código en tres zonas:

- `domain`: contiene el modelo `Price` y las excepciones de negocio.
- `application`: contiene el caso de uso y los puertos.
- `infrastructure`: contiene los detalles técnicos: REST, JPA, configuración, Swagger y manejo de errores HTTP.

El dominio no tiene dependencias de Spring, JPA ni de la capa REST. La aplicación define lo que necesita mediante puertos, y la infraestructura se encarga de implementarlos.

La estructura principal queda así:

```text
src/main/java/com/gft/prueba
|-- application
|   |-- port
|   |   |-- in
|   |   `-- out
|   `-- usecase
|-- domain
|   |-- exception
|   `-- model
`-- infrastructure
    |-- adapter
    |   |-- in/rest
    |   `-- out/persistence
    |-- config
    `-- exception
```

## Regla de negocio

Un precio aplica cuando coincide con:

- `brandId`
- `productId`
- `applicationDate` dentro del rango `[startDate, endDate]`

Si hay más de un precio aplicable, se elige:

1. El de mayor `priority`.
2. Si hubiera empate, el que tenga el `startDate` más reciente.

Esta ordenación se resuelve directamente en la consulta de persistencia para mantener el caso de uso simple.

## Cómo ejecutar la aplicación

Es necesario tener `JAVA_HOME` apuntando a un JDK 21.

En Linux o macOS:

```bash
./mvnw spring-boot:run
```

En Windows:

```bash
mvnw.cmd spring-boot:run
```

Para habilitar la consola H2 en local:

```bash
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

La aplicación arranca en:

```text
http://localhost:8080
```

## Endpoint principal

```http
GET /api/v1/prices?applicationDate=2020-06-14T10:00:00&productId=35455&brandId=1
```

Ejemplo de respuesta:

```json
{
  "productId": 35455,
  "brandId": 1,
  "priceList": 1,
  "startDate": "2020-06-14T00:00:00",
  "endDate": "2020-12-31T23:59:59",
  "finalPrice": 35.50,
  "currency": "EUR"
}
```

## Errores

Todas las respuestas de error mantienen el mismo formato:

```json
{
  "code": "PRICE_NOT_FOUND",
  "message": "No applicable price found",
  "timestamp": "2026-04-12T10:00:00"
}
```

Casos contemplados:

- `400`: parámetros obligatorios ausentes, ids no válidos o fecha con formato incorrecto.
- `404`: no existe un precio aplicable para la consulta.
- `500`: error inesperado.

## Base de datos

La aplicación usa H2 en memoria. El esquema y los datos iniciales se cargan al arrancar desde:

- `src/main/resources/schema.sql`
- `src/main/resources/data.sql`

La tabla `prices` incluye el índice compuesto `idx_prices_lookup`, orientado al patrón de búsqueda principal por marca, producto, rango de fechas y prioridad.

Consola H2:

```text
http://localhost:8080/h2-console
```

La consola H2 solo se habilita con el perfil `local`.

Datos de conexión:

```text
JDBC URL: jdbc:h2:mem:pricingdb
User: sa
Password:
```

## Swagger y contrato OpenAPI

Swagger UI está disponible en:

```text
http://localhost:8080/swagger-ui.html
```

El contrato OpenAPI se genera desde la aplicación y se expone en:

```text
http://localhost:8080/api-docs
```

## Observabilidad y resiliencia

La aplicación añade endurecimiento operacional mínimo:

- Timeouts acotados en Tomcat, Hikari y la transacción de lectura.
- Retry limitado solo ante errores transitorios de acceso a datos.
- Actuator expone `/actuator/health` y `/actuator/metrics`.
- Logs estructurados por request con `X-Correlation-Id`.

## Tests

Para ejecutar la suite completa:

```bash
./mvnw test
```

En Windows:

```bash
mvnw.cmd test
```

La suite incluye:

- Tests unitarios del caso de uso con el puerto de salida simulado.
- Tests funcionales con RestAssured levantando la aplicación en un puerto aleatorio.
- Test de persistencia para validar el desempate por `startDate` cuando la prioridad empata.
- Test de persistencia para validar el índice de consulta.
- Test de persistencia con volumen de datos.
- Validación de errores `400` y `404`.
- Validación de parámetro obligatorio ausente como caso KO.
- Validación de `X-Correlation-Id`, sanitización de valores inválidos y endpoints de salud/métricas.
- Validación de retry acotado ante errores transitorios de acceso a datos.
- Validación de que el contrato OpenAPI se expone correctamente.

Casos funcionales obligatorios cubiertos:

| Fecha | Tarifa esperada | Precio esperado |
| --- | ---: | ---: |
| `2020-06-14T10:00:00` | 1 | `35.50` |
| `2020-06-14T16:00:00` | 2 | `25.45` |
| `2020-06-14T21:00:00` | 1 | `35.50` |
| `2020-06-15T10:00:00` | 3 | `30.50` |
| `2020-06-16T21:00:00` | 4 | `38.95` |

## Docker

Primero se genera el JAR:

```bash
./mvnw clean package
```

Después se puede construir la imagen:

```bash
docker build -t pricing-api .
```

Y ejecutar el contenedor:

```bash
docker run --rm -p 8080:8080 pricing-api
```

## Decisiones tomadas

- He usado `LocalDateTime` porque el enunciado no requiere zonas horarias.
- El modelo de dominio es un `record` para mantenerlo pequeño e inmutable.
- No he usado Lombok porque en este caso no aporta lo suficiente.
- Hay un único caso de uso: obtener el precio aplicable.
- La selección por prioridad se delega en la consulta de base de datos para evitar lógica duplicada.
- El contrato OpenAPI se genera con springdoc para evitar desalineaciones con el controlador.
- La consola H2 queda limitada al perfil `local`.
- No he añadido circuit-breakers, bulkheads, rate limiting ni caché porque no hay integraciones externas ni un caso real que lo justifique en esta prueba.
