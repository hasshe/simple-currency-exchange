# Simple Currency Exchange

A Spring Boot REST API for currency exchange operations supporting SEK, EUR, and USD currencies.

## Features

- Get current exchange rates between supported currencies
- Exchange currency amounts with real-time rates
- Integration with Riksbanken API for exchange rate data
- H2 in-memory database for data persistence
- OpenAPI/Swagger documentation

## Technologies

- **Java 21**
- **Spring Boot 4.0.1**
- **Spring Data JPA**
- **H2 Database**
- **MapStruct** for object mapping
- **SpringDoc OpenAPI** for API documentation
- **Maven** for dependency management

## Supported Currencies

- SEK (Swedish Krona)
- EUR (Euro)
- USD (US Dollar)

## Prerequisites

- Java 21 or higher
- Maven 3.6+

## Getting Started

### Build the project

```bash
mvn clean install
```

### Run the application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### Get Current Exchange Rate

```http
GET /api/currency/current-rates/{currencyFrom}/{currencyTo}
```

**Example:**
```bash
curl http://localhost:8080/api/currency/current-rates/SEK/USD
```

### Exchange Currency

```http
POST /api/currency/exchange
Content-Type: application/json

{
  "currencyFrom": "SEK",
  "currencyTo": "USD",
  "amount": 100.0
}
```

**Example:**
```bash
curl -X POST http://localhost:8080/api/currency/exchange \
  -H "Content-Type: application/json" \
  -d '{"currencyFrom":"SEK","currencyTo":"USD","amount":100.0}'
```

## API Documentation

Once the application is running, access the interactive API documentation:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

## Database

The application uses H2 in-memory database. Access the H2 console at:

- **URL**: http://localhost:8080/h2-console
- **JDBC URL**: `jdbc:h2:file:./data/currencydb`
- **Username**: `sa`
- **Password**: (empty)
