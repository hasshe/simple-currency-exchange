package org.example.service;

import org.example.CurrencyEnum;
import org.example.controller.dto.responses.ExchangeRateResponse;
import org.example.controller.dto.responses.ExchangeResponse;

public interface CurrencyExchangeService {
    ExchangeRateResponse getLatestExchangeRate(CurrencyEnum currencyFrom, CurrencyEnum currencyTo);
    ExchangeResponse exchangeCurrency(CurrencyEnum currencyFrom, CurrencyEnum currencyTo, double amount);
}
