package org.example.external;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.external.dto.CalendarDayResponse;
import org.example.external.dto.CrossRateResponse;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;

@Component
public class RiksbankenApi {
    private static final String CALENDAR_DAYS_URL = "https://api.riksbank.se/swea/v1/CalendarDays";
    private static final String CROSS_RATES_URL = "https://api.riksbank.se/swea/v1/CrossRates";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public RiksbankenApi(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public List<CalendarDayResponse> getCalendarDays(LocalDate date) throws Exception {
        String formattedDate = date.toString();
        String url = CALENDAR_DAYS_URL + "/" + formattedDate;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.body(), new TypeReference<List<CalendarDayResponse>>() {});
    }

    public List<CrossRateResponse> getCrossRates(String seriesId1, String seriesId2, LocalDate from) throws Exception {
        String formattedDate = from.toString();
        String url = CROSS_RATES_URL + "/" + seriesId1 + "/" + seriesId2 + "/" + formattedDate;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.body(), new TypeReference<>() {
        });
    }
}
