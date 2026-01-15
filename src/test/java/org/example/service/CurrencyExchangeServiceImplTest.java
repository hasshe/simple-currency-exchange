package org.example.service;

import org.example.CurrencyEnum;
import org.example.controller.dto.responses.ExchangeRateResponse;
import org.example.controller.dto.responses.ExchangeResponse;
import org.example.external.RiksbankenApi;
import org.example.external.dto.CalendarDayResponse;
import org.example.external.dto.CrossRateResponse;
import org.example.mapper.ExchangeRateMapper;
import org.example.repository.CurrencyExchangeRepository;
import org.example.repository.entities.ExchangeRate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrencyExchangeServiceImplTest {

    @Mock
    private ExchangeRateMapper mapper;

    @Mock
    private CurrencyExchangeRepository repository;

    @Mock
    private RiksbankenApi riksbankenApi;

    @InjectMocks
    private CurrencyExchangeServiceImpl service;

    private CurrencyEnum currencyFrom;
    private CurrencyEnum currencyTo;
    private LocalDate today;
    private ExchangeRate mockExchangeRate;
    private ExchangeRateResponse mockExchangeRateResponse;

    @BeforeEach
    void setUp() {
        currencyFrom = CurrencyEnum.USD;
        currencyTo = CurrencyEnum.EUR;
        today = LocalDate.now();

        mockExchangeRate = new ExchangeRate(currencyFrom, 0.85, today, currencyTo);
        mockExchangeRateResponse = new ExchangeRateResponse(CurrencyEnum.USD.name(), CurrencyEnum.EUR.name(), 0.85, today.toString());
    }

    @Test
    void getLatestExchangeRate_shouldReturnFromDatabase_whenTodayRateExists() {
        // Given
        when(repository.findLatestExchangeRateByCurrencies(currencyFrom, currencyTo))
                .thenReturn(mockExchangeRate);
        when(mapper.toDto(mockExchangeRate)).thenReturn(mockExchangeRateResponse);

        // When
        ExchangeRateResponse result = service.getLatestExchangeRate(currencyFrom, currencyTo);

        // Then
        assertNotNull(result);
        assertEquals(CurrencyEnum.USD.name(), result.currencyFrom());
        assertEquals(CurrencyEnum.EUR.name(), result.currencyTo());
        assertEquals(0.85, result.rate());
        verify(repository).findLatestExchangeRateByCurrencies(currencyFrom, currencyTo);
        verify(mapper).toDto(mockExchangeRate);
        verifyNoInteractions(riksbankenApi);
    }

    @Test
    void getLatestExchangeRate_shouldFetchFromApi_whenTodayIsBankDayAndAfterCutoff() throws Exception {
        // Given
        ExchangeRate oldRate = new ExchangeRate(currencyFrom, 0.84, today.minusDays(1), currencyTo);
        when(repository.findLatestExchangeRateByCurrencies(currencyFrom, currencyTo))
                .thenReturn(oldRate);

        CalendarDayResponse bankDay = new CalendarDayResponse(today.toString(), true, 2024, 1, 1, false);
        when(riksbankenApi.getCalendarDays(any(LocalDate.class)))
                .thenReturn(List.of(bankDay));

        CrossRateResponse crossRate = new CrossRateResponse(today.toString(), 0.86);
        when(riksbankenApi.getCrossRates(
                eq(currencyFrom.getCurrencyCode()),
                eq(currencyTo.getCurrencyCode()),
                any(LocalDate.class)))
                .thenReturn(List.of(crossRate));

        // When
        ExchangeRateResponse result = service.getLatestExchangeRate(currencyFrom, currencyTo);

        // Then
        assertNotNull(result);
        assertEquals(CurrencyEnum.USD.name(), result.currencyFrom());
        assertEquals(CurrencyEnum.EUR.name(), result.currencyTo());
        assertEquals(0.86, result.rate());
        assertEquals(today.toString(), result.latestRateDate());
        verify(repository).save(any(ExchangeRate.class));
    }

    @Test
    void getLatestExchangeRate_shouldFetchFromPastWeek_whenTodayIsNotBankDay() throws Exception {
        // Given
        when(repository.findLatestExchangeRateByCurrencies(currencyFrom, currencyTo))
                .thenReturn(null);

        CalendarDayResponse nonBankDay = new CalendarDayResponse(today.toString(), false, 2024, 1, 1, false);
        when(riksbankenApi.getCalendarDays(any(LocalDate.class)))
                .thenReturn(List.of(nonBankDay));

        LocalDate lastWeek = today.minusDays(2);
        CrossRateResponse crossRate = new CrossRateResponse(lastWeek.toString(), 0.85);
        when(riksbankenApi.getCrossRates(
                eq(currencyFrom.getCurrencyCode()),
                eq(currencyTo.getCurrencyCode()),
                any(LocalDate.class)))
                .thenReturn(List.of(crossRate));

        // When
        ExchangeRateResponse result = service.getLatestExchangeRate(currencyFrom, currencyTo);

        // Then
        assertNotNull(result);
        assertEquals(CurrencyEnum.USD.name(), result.currencyFrom());
        assertEquals(CurrencyEnum.EUR.name(), result.currencyTo());
        assertEquals(0.85, result.rate());
        verify(repository).save(any(ExchangeRate.class));
    }

    @Test
    void getLatestExchangeRate_shouldReturnNull_whenNoRateAvailable() throws Exception {
        // Given
        when(repository.findLatestExchangeRateByCurrencies(currencyFrom, currencyTo))
                .thenReturn(null);

        CalendarDayResponse nonBankDay = new CalendarDayResponse(today.toString(), false, 2024, 1, 1, false);
        when(riksbankenApi.getCalendarDays(any(LocalDate.class)))
                .thenReturn(List.of(nonBankDay));

        when(riksbankenApi.getCrossRates(
                eq(currencyFrom.getCurrencyCode()),
                eq(currencyTo.getCurrencyCode()),
                any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // When
        ExchangeRateResponse result = service.getLatestExchangeRate(currencyFrom, currencyTo);

        // Then
        assertNull(result);
        verify(repository, never()).save(any(ExchangeRate.class));
    }

    @Test
    void getLatestExchangeRate_shouldReturnDbRate_whenApiReturnsOlderRate() throws Exception {
        // Given
        LocalDate oldDate = today.minusDays(5);
        ExchangeRate dbRate = new ExchangeRate(currencyFrom, 0.85, today.minusDays(2), currencyTo);
        when(repository.findLatestExchangeRateByCurrencies(currencyFrom, currencyTo))
                .thenReturn(dbRate);

        CalendarDayResponse nonBankDay = new CalendarDayResponse(today.toString(), false, 2024, 1, 1, false);
        when(riksbankenApi.getCalendarDays(any(LocalDate.class)))
                .thenReturn(List.of(nonBankDay));

        CrossRateResponse olderCrossRate = new CrossRateResponse(oldDate.toString(), 0.84);
        when(riksbankenApi.getCrossRates(
                eq(currencyFrom.getCurrencyCode()),
                eq(currencyTo.getCurrencyCode()),
                any(LocalDate.class)))
                .thenReturn(List.of(olderCrossRate));

        when(mapper.toDto(dbRate)).thenReturn(new ExchangeRateResponse(CurrencyEnum.USD.name(), CurrencyEnum.EUR.name(), 0.85, dbRate.getLatestRateDate().toString()));

        // When
        ExchangeRateResponse result = service.getLatestExchangeRate(currencyFrom, currencyTo);

        // Then
        assertNotNull(result);
        assertEquals(0.85, result.rate());
        verify(mapper).toDto(dbRate);
        verify(repository, never()).save(any(ExchangeRate.class));
    }

    @Test
    void getLatestExchangeRate_shouldHandleApiException_whenCalendarDaysFails() throws Exception {
        // Given
        ExchangeRate oldRate = new ExchangeRate(currencyFrom, 0.85, today.minusDays(1), currencyTo);
        when(repository.findLatestExchangeRateByCurrencies(currencyFrom, currencyTo))
                .thenReturn(oldRate);
        when(riksbankenApi.getCalendarDays(any(LocalDate.class)))
                .thenThrow(new RuntimeException("API error"));
        when(mapper.toDto(oldRate)).thenReturn(mockExchangeRateResponse);

        // When
        ExchangeRateResponse result = service.getLatestExchangeRate(currencyFrom, currencyTo);

        // Then
        assertNotNull(result);
        assertEquals(0.85, result.rate());
        verify(mapper).toDto(oldRate);
    }

    @Test
    void getLatestExchangeRate_shouldHandleApiException_whenCrossRatesFails() throws Exception {
        // Given
        when(repository.findLatestExchangeRateByCurrencies(currencyFrom, currencyTo))
                .thenReturn(null);

        CalendarDayResponse nonBankDay = new CalendarDayResponse(today.toString(), false, 2024, 1, 1, false);
        when(riksbankenApi.getCalendarDays(any(LocalDate.class)))
                .thenReturn(List.of(nonBankDay));

        when(riksbankenApi.getCrossRates(
                eq(currencyFrom.getCurrencyCode()),
                eq(currencyTo.getCurrencyCode()),
                any(LocalDate.class)))
                .thenThrow(new RuntimeException("API error"));

        // When
        ExchangeRateResponse result = service.getLatestExchangeRate(currencyFrom, currencyTo);

        // Then
        assertNull(result);
    }

    @Test
    void getLatestExchangeRate_shouldSelectLatestRate_whenMultipleRatesFromPastWeek() throws Exception {
        // Given
        when(repository.findLatestExchangeRateByCurrencies(currencyFrom, currencyTo))
                .thenReturn(null);

        CalendarDayResponse nonBankDay = new CalendarDayResponse(today.toString(), false, 2024, 1, 1, false);
        when(riksbankenApi.getCalendarDays(any(LocalDate.class)))
                .thenReturn(List.of(nonBankDay));

        LocalDate date1 = today.minusDays(5);
        LocalDate date2 = today.minusDays(2);
        LocalDate date3 = today.minusDays(7);

        List<CrossRateResponse> rates = List.of(
                new CrossRateResponse(date1.toString(), 0.83),
                new CrossRateResponse(date2.toString(), 0.85),
                new CrossRateResponse(date3.toString(), 0.82)
        );

        when(riksbankenApi.getCrossRates(
                eq(currencyFrom.getCurrencyCode()),
                eq(currencyTo.getCurrencyCode()),
                any(LocalDate.class)))
                .thenReturn(rates);

        // When
        ExchangeRateResponse result = service.getLatestExchangeRate(currencyFrom, currencyTo);

        // Then
        assertNotNull(result);
        assertEquals(0.85, result.rate());
        assertEquals(date2.toString(), result.latestRateDate());
        verify(repository).save(any(ExchangeRate.class));
    }

    @Test
    void exchangeCurrency_shouldReturnExchangeResponse_whenRateExists() {
        // Given
        double amount = 100.0;
        when(repository.findLatestExchangeRateByCurrencies(currencyFrom, currencyTo))
                .thenReturn(mockExchangeRate);
        when(mapper.toDto(mockExchangeRate)).thenReturn(mockExchangeRateResponse);

        // When
        ExchangeResponse result = service.exchangeCurrency(currencyFrom, currencyTo, amount);

        // Then
        assertNotNull(result);
        assertEquals(currencyFrom, result.currencyFrom());
        assertEquals(currencyTo, result.currencyTo());
        assertEquals(100.0, result.originalAmount());
        assertEquals(85.0, result.exchangedAmount());
        assertEquals(0.85, result.exchangeRate());
    }

    @Test
    void exchangeCurrency_shouldReturnNull_whenNoRateExists() {
        // Given
        double amount = 100.0;
        when(repository.findLatestExchangeRateByCurrencies(currencyFrom, currencyTo))
                .thenReturn(null);

        try {
            when(riksbankenApi.getCalendarDays(any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());
            when(riksbankenApi.getCrossRates(anyString(), anyString(), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());
        } catch (Exception e) {
            fail("Setup should not throw exception");
        }

        // When
        ExchangeResponse result = service.exchangeCurrency(currencyFrom, currencyTo, amount);

        // Then
        assertNull(result);
    }

    @Test
    void exchangeCurrency_shouldCalculateCorrectly_withZeroAmount() {
        // Given
        double amount = 0.0;
        when(repository.findLatestExchangeRateByCurrencies(currencyFrom, currencyTo))
                .thenReturn(mockExchangeRate);
        when(mapper.toDto(mockExchangeRate)).thenReturn(mockExchangeRateResponse);

        // When
        ExchangeResponse result = service.exchangeCurrency(currencyFrom, currencyTo, amount);

        // Then
        assertNotNull(result);
        assertEquals(0.0, result.originalAmount());
        assertEquals(0.0, result.exchangedAmount());
        assertEquals(0.85, result.exchangeRate());
    }

    @Test
    void exchangeCurrency_shouldCalculateCorrectly_withLargeAmount() {
        // Given
        double amount = 1000000.0;
        when(repository.findLatestExchangeRateByCurrencies(currencyFrom, currencyTo))
                .thenReturn(mockExchangeRate);
        when(mapper.toDto(mockExchangeRate)).thenReturn(mockExchangeRateResponse);

        // When
        ExchangeResponse result = service.exchangeCurrency(currencyFrom, currencyTo, amount);

        // Then
        assertNotNull(result);
        assertEquals(1000000.0, result.originalAmount());
        assertEquals(850000.0, result.exchangedAmount());
        assertEquals(0.85, result.exchangeRate());
    }
}
