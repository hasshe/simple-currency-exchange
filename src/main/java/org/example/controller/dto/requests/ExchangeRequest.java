package org.example.controller.dto.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.example.CurrencyEnum;

public record ExchangeRequest(@NotNull CurrencyEnum currencyFrom, @NotNull CurrencyEnum currencyTo, @Min(0) double amount) {
}
