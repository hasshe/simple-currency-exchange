package org.example.controller.dto.responses;

import org.example.CurrencyEnum;

public record ExchangeResponse(CurrencyEnum currencyFrom, CurrencyEnum currencyTo,
                               double originalAmount, double exchangedAmount,
                               double exchangeRate) {
}
