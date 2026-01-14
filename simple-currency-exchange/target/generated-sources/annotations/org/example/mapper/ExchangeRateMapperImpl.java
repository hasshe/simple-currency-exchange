package org.example.mapper;

import java.time.format.DateTimeFormatter;
import javax.annotation.processing.Generated;
import org.example.controller.dto.responses.ExchangeRateResponse;
import org.example.repository.entities.ExchangeRate;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-14T21:06:59+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.3 (Eclipse Adoptium)"
)
@Component
public class ExchangeRateMapperImpl implements ExchangeRateMapper {

    @Override
    public ExchangeRateResponse toDto(ExchangeRate exchangeRate) {
        if ( exchangeRate == null ) {
            return null;
        }

        String currencyFrom = null;
        String currencyTo = null;
        double rate = 0.0d;
        String latestRateDate = null;

        if ( exchangeRate.getCurrencyFrom() != null ) {
            currencyFrom = exchangeRate.getCurrencyFrom().name();
        }
        if ( exchangeRate.getCurrencyTo() != null ) {
            currencyTo = exchangeRate.getCurrencyTo().name();
        }
        if ( exchangeRate.getRate() != null ) {
            rate = exchangeRate.getRate();
        }
        if ( exchangeRate.getLatestRateDate() != null ) {
            latestRateDate = DateTimeFormatter.ISO_LOCAL_DATE.format( exchangeRate.getLatestRateDate() );
        }

        ExchangeRateResponse exchangeRateResponse = new ExchangeRateResponse( currencyFrom, currencyTo, rate, latestRateDate );

        return exchangeRateResponse;
    }
}
