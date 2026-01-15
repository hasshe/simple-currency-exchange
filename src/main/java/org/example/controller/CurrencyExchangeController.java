package org.example.controller;

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
public class CurrencyExchangeController {

    private final CurrencyExchangeService currencyExchangeService;

    public CurrencyExchangeController(CurrencyExchangeService currencyExchangeService) {
        this.currencyExchangeService = currencyExchangeService;
    }

    @GetMapping("/current-rates/{currencyFrom}/{currencyTo}")
    public ResponseEntity<ExchangeRateResponse> getLatestExchangeRates(@PathVariable CurrencyEnum currencyFrom, @PathVariable CurrencyEnum currencyTo) {
        return ResponseEntity.ok(currencyExchangeService.getLatestExchangeRate(currencyFrom, currencyTo));
    }

    @PostMapping(value = "/exchange")
    public ResponseEntity<ExchangeResponse> exchangeCurrency(@RequestBody @NotNull @Valid ExchangeRequest request) {
        return ResponseEntity.ok(currencyExchangeService.exchangeCurrency(request.currencyFrom(), request.currencyTo(), request.amount()));
    }
}

// TODO: add tests unit + integration
// TODO: Extra - dockerfile
// TODO: OpenApi documentation with swagger
// TODO: Readme.md
