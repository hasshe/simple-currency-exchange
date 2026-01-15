package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.example.CurrencyEnum;
import org.example.controller.dto.requests.ExchangeRequest;
import org.example.controller.dto.responses.ExchangeRateResponse;
import org.example.controller.dto.responses.ExchangeResponse;
import org.example.service.CurrencyExchangeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/currency")
@Tag(name = "Currency Exchange", description = "Currency exchange rate and conversion operations")
public class CurrencyExchangeController {

    private final CurrencyExchangeService currencyExchangeService;

    public CurrencyExchangeController(CurrencyExchangeService currencyExchangeService) {
        this.currencyExchangeService = currencyExchangeService;
    }

    @Operation(summary = "Get current exchange rate", description = "Retrieves the latest exchange rate between two currencies")
    @GetMapping("/current-rates/{currencyFrom}/{currencyTo}")
    public ResponseEntity<ExchangeRateResponse> getLatestExchangeRates(
            @Parameter(description = "Source currency code") @PathVariable CurrencyEnum currencyFrom,
            @Parameter(description = "Target currency code") @PathVariable CurrencyEnum currencyTo) {
        return ResponseEntity.ok(currencyExchangeService.getLatestExchangeRate(currencyFrom, currencyTo));
    }

    @Operation(summary = "Exchange currency", description = "Converts an amount from one currency to another using the latest exchange rate")
    @PostMapping(value = "/exchange")
    public ResponseEntity<ExchangeResponse> exchangeCurrency(
            @Parameter(description = "Exchange request with source currency, target currency, and amount") @RequestBody @NotNull @Valid ExchangeRequest request) {
        return ResponseEntity.ok(currencyExchangeService.exchangeCurrency(request.currencyFrom(), request.currencyTo(), request.amount()));
    }
}
