package org.example.service;

import org.example.CurrencyEnum;
import org.example.controller.dto.responses.ExchangeRateResponse;

public interface CurrencyExchangeService {
    ExchangeRateResponse getLatestExchangeRate(CurrencyEnum currencyFrom, CurrencyEnum currencyTo);
}
