package org.example.controller.dto.responses;

public record ExchangeResponse(String currencyFrom, String currencyTo,
                               double originalAmount, double exchangedAmount,
                               double exchangeRate) {
}
