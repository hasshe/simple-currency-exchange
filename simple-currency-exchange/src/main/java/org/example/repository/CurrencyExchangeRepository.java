package org.example.repository;

import org.example.CurrencyEnum;
import org.example.repository.entities.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrencyExchangeRepository extends JpaRepository<ExchangeRate, Long> {
    @Query("SELECT e FROM ExchangeRate e ORDER BY e.id DESC LIMIT 1")
    ExchangeRate findLatestExchangeRate();

    @Query("SELECT e FROM ExchangeRate e WHERE e.currencyFrom = :currencyFrom AND e.currencyTo = :currencyTo ORDER BY e.latestRateDate DESC, e.id DESC LIMIT 1")
    ExchangeRate findLatestExchangeRateByCurrencies(CurrencyEnum currencyFrom, CurrencyEnum currencyTo);
}
