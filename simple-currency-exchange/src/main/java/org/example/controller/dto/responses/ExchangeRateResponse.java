package org.example.controller.dto.responses;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ExchangeRateResponse(@NotBlank String currencyFrom, @NotBlank String currencyTo, @Min(0) double rate, @NotBlank String latestRateDate) {
}
