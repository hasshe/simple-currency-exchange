package org.example.mapper;

import java.time.LocalDate;
import org.example.CurrencyEnum;
import org.example.controller.dto.responses.ExchangeRateResponse;
import org.example.repository.entities.ExchangeRate;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ExchangeRateMapperTest {

    private final ExchangeRateMapper mapper = Mappers.getMapper(ExchangeRateMapper.class);

    @Test
    void toDto_shouldMapExchangeRateToExchangeRateResponse() {
        // Given
        LocalDate now = LocalDate.now();
        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setCurrencyFrom(CurrencyEnum.USD);
        exchangeRate.setCurrencyTo(CurrencyEnum.SEK);
        exchangeRate.setRate(10.5);
        exchangeRate.setLatestRateDate(now);

        // When
        ExchangeRateResponse response = mapper.toDto(exchangeRate);

        // Then
        assertEquals(CurrencyEnum.USD.name(), response.currencyFrom());
        assertEquals(CurrencyEnum.SEK.name(), response.currencyTo());
        assertEquals(10.5, response.rate());
        assertEquals(now.toString(), response.latestRateDate());
    }

    @Test
    void toDto_shouldReturnNullWhenInputIsNull() {
        // When
        ExchangeRateResponse response = mapper.toDto(null);

        // Then
        assertNull(response);
    }
}
