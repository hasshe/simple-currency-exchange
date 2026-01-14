package org.example.repository.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import org.example.CurrencyEnum;

@Entity
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate latestRateDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CurrencyEnum currencyFrom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CurrencyEnum currencyTo;

    @Column(nullable = false)
    private Double rate;

    public ExchangeRate() {
    }

    public ExchangeRate(CurrencyEnum currencyFrom, Double rate, LocalDate latestRateDate, CurrencyEnum currencyTo) {
        this.currencyFrom = currencyFrom;
        this.rate = rate;
        this.latestRateDate = latestRateDate;
        this.currencyTo = currencyTo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getLatestRateDate() {
        return latestRateDate;
    }

    public void setLatestRateDate(LocalDate latestRateDate) {
        this.latestRateDate = latestRateDate;
    }

    public CurrencyEnum getCurrencyFrom() {
        return currencyFrom;
    }

    public void setCurrencyFrom(CurrencyEnum currencyFrom) {
        this.currencyFrom = currencyFrom;
    }

    public CurrencyEnum getCurrencyTo() {
        return currencyTo;
    }

    public void setCurrencyTo(CurrencyEnum currencyTo) {
        this.currencyTo = currencyTo;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }
}
