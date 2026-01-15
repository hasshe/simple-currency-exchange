package org.example.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

        ExchangeRate latestRate = repository.findLatestExchangeRateByCurrencies(currencyFrom, currencyTo);
        if (latestRate != null && latestRate.getLatestRateDate().equals(today)) {
            return mapper.toDto(latestRate);
        }

        ExchangeRateResponse rateResponse = checkIfShouldGetAndSaveLatestExchangeRate(currencyFrom, currencyTo, currentTime, cutoffTime, today);
        if (rateResponse != null) return rateResponse;
        return mapper.toDto(latestRate != null ? latestRate : repository.findLatestExchangeRate());
    }

    private ExchangeRateResponse checkIfShouldGetAndSaveLatestExchangeRate(CurrencyEnum currencyFrom, CurrencyEnum currencyTo, LocalTime currentTime, LocalTime cutoffTime, LocalDate today) {
        List<CalendarDayResponse> resp = getLatestExchangeRateFromApi();
        boolean isBankDay = resp.getFirst().swedishBankday();
        boolean isTimeAfter1615 = currentTime.isAfter(cutoffTime);

        if (isBankDay && isTimeAfter1615) {
            CrossRateResponse crossRate = getCrossRatesFromApi(currencyFrom.getCurrencyCode(), currencyTo.getCurrencyCode(), today).getFirst();

            ExchangeRate newRate = new ExchangeRate(currencyFrom, crossRate.value(), LocalDate.parse(crossRate.date()), currencyTo);
            repository.save(newRate);

            return new ExchangeRateResponse(currencyFrom.name(), currencyTo.name(), crossRate.value(), crossRate.date());
        }
        return null;
    }

    private List<CalendarDayResponse> getLatestExchangeRateFromApi() {
        try {
            return riksbankenApi.getCalendarDays(LocalDate.now());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<CrossRateResponse> getCrossRatesFromApi(String seriesId1, String seriesId2, LocalDate from) {
        try {
            return riksbankenApi.getCrossRates(seriesId1, seriesId2, from);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
