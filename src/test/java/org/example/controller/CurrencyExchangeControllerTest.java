package org.example.controller;

import org.example.CurrencyEnum;
import org.example.controller.dto.requests.ExchangeRequest;
import org.example.controller.dto.responses.ExchangeRateResponse;
import org.example.controller.dto.responses.ExchangeResponse;
import org.example.service.CurrencyExchangeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyExchangeControllerTest {

    @Mock
    private CurrencyExchangeService currencyExchangeService;

    @InjectMocks
    private CurrencyExchangeController currencyExchangeController;

    @Test
    void getLatestExchangeRates_shouldReturnExchangeRate_whenValidCurrenciesProvided() {
        // Given
        CurrencyEnum currencyFrom = CurrencyEnum.USD;
        CurrencyEnum currencyTo = CurrencyEnum.EUR;
        ExchangeRateResponse expectedResponse = new ExchangeRateResponse(
                "USD",
                "EUR",
                0.85,
                "2024-01-15"
        );

        when(currencyExchangeService.getLatestExchangeRate(currencyFrom, currencyTo))
                .thenReturn(expectedResponse);

        // When
        ResponseEntity<ExchangeRateResponse> response = currencyExchangeController.getLatestExchangeRates(currencyFrom, currencyTo);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("USD", response.getBody().currencyFrom());
        assertEquals("EUR", response.getBody().currencyTo());
        assertEquals(0.85, response.getBody().rate());
        assertEquals("2024-01-15", response.getBody().latestRateDate());

        verify(currencyExchangeService).getLatestExchangeRate(currencyFrom, currencyTo);
    }

    @Test
    void getLatestExchangeRates_shouldReturnExchangeRate_whenDifferentCurrencyPairProvided() {
        // Given
        CurrencyEnum currencyFrom = CurrencyEnum.SEK;
        CurrencyEnum currencyTo = CurrencyEnum.USD;
        ExchangeRateResponse expectedResponse = new ExchangeRateResponse(
                "SEK",
                "USD",
                1.27,
                "2024-01-15"
        );

        when(currencyExchangeService.getLatestExchangeRate(currencyFrom, currencyTo))
                .thenReturn(expectedResponse);

        // When
        ResponseEntity<ExchangeRateResponse> response = currencyExchangeController.getLatestExchangeRates(currencyFrom, currencyTo);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SEK", response.getBody().currencyFrom());
        assertEquals("USD", response.getBody().currencyTo());
        assertEquals(1.27, response.getBody().rate());

        verify(currencyExchangeService).getLatestExchangeRate(currencyFrom, currencyTo);
    }

    @Test
    void exchangeCurrency_shouldReturnExchangeResponse_whenValidRequestProvided() {
        // Given
        ExchangeRequest request = new ExchangeRequest(CurrencyEnum.USD, CurrencyEnum.EUR, 100.0);
        ExchangeResponse expectedResponse = new ExchangeResponse(
                CurrencyEnum.USD,
                CurrencyEnum.EUR,
                100.0,
                85.0,
                0.85
        );

        when(currencyExchangeService.exchangeCurrency(CurrencyEnum.USD, CurrencyEnum.EUR, 100.0))
                .thenReturn(expectedResponse);

        // When
        ResponseEntity<ExchangeResponse> response = currencyExchangeController.exchangeCurrency(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(CurrencyEnum.USD, response.getBody().currencyFrom());
        assertEquals(CurrencyEnum.EUR, response.getBody().currencyTo());
        assertEquals(100.0, response.getBody().originalAmount());
        assertEquals(85.0, response.getBody().exchangedAmount());
        assertEquals(0.85, response.getBody().exchangeRate());

        verify(currencyExchangeService).exchangeCurrency(CurrencyEnum.USD, CurrencyEnum.EUR, 100.0);
    }

    @Test
    void exchangeCurrency_shouldReturnExchangeResponse_whenLargeAmountProvided() {
        // Given
        ExchangeRequest request = new ExchangeRequest(CurrencyEnum.EUR, CurrencyEnum.USD, 10000.0);
        ExchangeResponse expectedResponse = new ExchangeResponse(
                CurrencyEnum.EUR,
                CurrencyEnum.USD,
                10000.0,
                8600.0,
                0.86
        );

        when(currencyExchangeService.exchangeCurrency(CurrencyEnum.EUR, CurrencyEnum.USD, 10000.0))
                .thenReturn(expectedResponse);

        // When
        ResponseEntity<ExchangeResponse> response = currencyExchangeController.exchangeCurrency(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(CurrencyEnum.EUR, response.getBody().currencyFrom());
        assertEquals(CurrencyEnum.USD, response.getBody().currencyTo());
        assertEquals(10000.0, response.getBody().originalAmount());
        assertEquals(8600.0, response.getBody().exchangedAmount());
        assertEquals(0.86, response.getBody().exchangeRate());

        verify(currencyExchangeService).exchangeCurrency(CurrencyEnum.EUR, CurrencyEnum.USD, 10000.0);
    }

    @Test
    void exchangeCurrency_shouldReturnExchangeResponse_whenZeroAmountProvided() {
        // Given
        ExchangeRequest request = new ExchangeRequest(CurrencyEnum.USD, CurrencyEnum.EUR, 0.0);
        ExchangeResponse expectedResponse = new ExchangeResponse(
                CurrencyEnum.USD,
                CurrencyEnum.EUR,
                0.0,
                0.0,
                0.85
        );

        when(currencyExchangeService.exchangeCurrency(CurrencyEnum.USD, CurrencyEnum.EUR, 0.0))
                .thenReturn(expectedResponse);

        // When
        ResponseEntity<ExchangeResponse> response = currencyExchangeController.exchangeCurrency(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0.0, response.getBody().originalAmount());
        assertEquals(0.0, response.getBody().exchangedAmount());

        verify(currencyExchangeService).exchangeCurrency(CurrencyEnum.USD, CurrencyEnum.EUR, 0.0);
    }

    @Test
    void exchangeCurrency_shouldHandleBadInput_whenNegativeAmountProvided() {
        // Given
        ExchangeRequest request = new ExchangeRequest(CurrencyEnum.USD, CurrencyEnum.EUR, -100.0);

        when(currencyExchangeService.exchangeCurrency(CurrencyEnum.USD, CurrencyEnum.EUR, -100.0))
                .thenThrow(new IllegalArgumentException("Amount must be non-negative"));

        // When/Then
        try {
            currencyExchangeController.exchangeCurrency(request);
        } catch (IllegalArgumentException e) {
            assertEquals("Amount must be non-negative", e.getMessage());
        }

        verify(currencyExchangeService).exchangeCurrency(CurrencyEnum.USD, CurrencyEnum.EUR, -100.0);
    }
}
