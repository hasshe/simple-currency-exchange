package org.example.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import org.example.CurrencyEnum;
import org.example.controller.dto.responses.ExchangeRateResponse;
import org.example.external.dto.CalendarDayResponse;
import org.example.external.dto.CrossRateResponse;
import org.example.external.RiksbankenApi;
import org.example.mapper.ExchangeRateMapper;
import org.example.repository.CurrencyExchangeRepository;
import org.example.repository.entities.ExchangeRate;
import org.springframework.stereotype.Service;

@Service
public class CurrencyExchangeServiceImpl implements CurrencyExchangeService {

    public static final String EUROPE_STOCKHOLM = "Europe/Stockholm";
    public static final int HOUR = 16;
    public static final int MINUTE = 15;
    private final ExchangeRateMapper mapper;
    private final CurrencyExchangeRepository repository;
    private final RiksbankenApi riksbankenApi;

    public CurrencyExchangeServiceImpl(ExchangeRateMapper mapper, CurrencyExchangeRepository repository, RiksbankenApi riksbankenApi) {
        this.mapper = mapper;
        this.repository = repository;
        this.riksbankenApi = riksbankenApi;
    }

    @Override
    public ExchangeRateResponse getLatestExchangeRate(CurrencyEnum currencyFrom, CurrencyEnum currencyTo) {
        ZonedDateTime stockholmTime = ZonedDateTime.now(ZoneId.of(EUROPE_STOCKHOLM));
        LocalDate today = stockholmTime.toLocalDate();
        LocalTime currentTime = stockholmTime.toLocalTime();
        LocalTime cutoffTime = LocalTime.of(HOUR, MINUTE);

        ExchangeRate latestRateInDb = repository.findLatestExchangeRateByCurrencies(currencyFrom, currencyTo);

        if (latestRateInDb != null && latestRateInDb.getLatestRateDate().equals(today)) {
            return mapper.toDto(latestRateInDb);
        }

        boolean isTodayBankDay = isBankDay(today);
        boolean isAfterCutoff = currentTime.isAfter(cutoffTime) || currentTime.equals(cutoffTime);

        ExchangeRateResponse latestRate = fetchLatestRate(currencyFrom, currencyTo, isTodayBankDay, isAfterCutoff, today);
        if (latestRate != null) return latestRate;

        ExchangeRateResponse latestRateFromPastWeek = fetchLatestRateFromPastWeek(currencyFrom, currencyTo, latestRateInDb, isTodayBankDay, isAfterCutoff, today);
        if (latestRateFromPastWeek != null) return latestRateFromPastWeek;

        return latestRateInDb != null ? mapper.toDto(latestRateInDb) : null;
    }

    private ExchangeRateResponse fetchLatestRate(CurrencyEnum currencyFrom, CurrencyEnum currencyTo, boolean isTodayBankDay, boolean isAfterCutoff, LocalDate today) {
        if (isTodayBankDay && isAfterCutoff) {
            CrossRateResponse todayRate = fetchAndSaveExchangeRate(currencyFrom, currencyTo, today);
            if (todayRate != null) {
                return new ExchangeRateResponse(currencyFrom.name(), currencyTo.name(), todayRate.value(), todayRate.date());
            }
        }
        return null;
    }

    private ExchangeRateResponse fetchLatestRateFromPastWeek(CurrencyEnum currencyFrom, CurrencyEnum currencyTo, ExchangeRate latestRateInDb, boolean isTodayBankDay, boolean isAfterCutoff, LocalDate today) {
        if (latestRateInDb == null || (!isTodayBankDay || !isAfterCutoff)) {
            CrossRateResponse latestRate = fetchLatestAvailableRate(currencyFrom, currencyTo, today);
            if (latestRate != null) {
                if (latestRateInDb == null || LocalDate.parse(latestRate.date()).isAfter(latestRateInDb.getLatestRateDate())) {
                    saveExchangeRate(currencyFrom, currencyTo, latestRate);
                }
                return new ExchangeRateResponse(currencyFrom.name(), currencyTo.name(), latestRate.value(), latestRate.date());
            }
        }
        return null;
    }

    private boolean isBankDay(LocalDate date) {
        try {
            List<CalendarDayResponse> response = riksbankenApi.getCalendarDays(date);
            return !response.isEmpty() && response.getFirst().swedishBankday();
        } catch (Exception e) {
            return false;
        }
    }

    private CrossRateResponse fetchAndSaveExchangeRate(CurrencyEnum currencyFrom, CurrencyEnum currencyTo, LocalDate date) {
        try {
            List<CrossRateResponse> rates = riksbankenApi.getCrossRates(
                    currencyFrom.getCurrencyCode(),
                    currencyTo.getCurrencyCode(),
                    date
            );
            if (!rates.isEmpty()) {
                CrossRateResponse rate = rates.getFirst();
                saveExchangeRate(currencyFrom, currencyTo, rate);
                return rate;
            }
        } catch (Exception e) {
            // TODO: add logging
        }
        return null;
    }

    private CrossRateResponse fetchLatestAvailableRate(CurrencyEnum currencyFrom, CurrencyEnum currencyTo, LocalDate today) {
        try {
            LocalDate fromDate = today.minusDays(7);
            List<CrossRateResponse> rates = riksbankenApi.getCrossRates(
                    currencyFrom.getCurrencyCode(),
                    currencyTo.getCurrencyCode(),
                    fromDate
            );
            return rates.stream()
                    .max(Comparator.comparing(rateResponse -> LocalDate.parse(rateResponse.date())))
                    .orElse(null);
        } catch (Exception e) {
            // TODO: add logging
        }
        return null;
    }

    private void saveExchangeRate(CurrencyEnum currencyFrom, CurrencyEnum currencyTo, CrossRateResponse crossRate) {
        ExchangeRate newRate = new ExchangeRate(
                currencyFrom,
                crossRate.value(),
                LocalDate.parse(crossRate.date()),
                currencyTo
        );
        repository.save(newRate);
    }
}
