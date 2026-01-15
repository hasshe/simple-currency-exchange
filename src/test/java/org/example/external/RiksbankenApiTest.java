package org.example.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;
import org.example.external.dto.CalendarDayResponse;
import org.example.external.dto.CrossRateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiksbankenApiTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private RiksbankenApi riksbankenApi;

    @BeforeEach
    void setUp() {
        riksbankenApi = new RiksbankenApi(httpClient, objectMapper);
    }

    @Test
    void getCalendarDays_shouldReturnListOfCalendarDayResponse() throws Exception {
        // Given
        LocalDate date = LocalDate.of(2024, 1, 1);
        String jsonResponse = "[{\"calendarDate\":\"2024-01-01\",\"swedishBankday\":false,\"weekYear\":2024,\"weekNumber\":1,\"quarterNumber\":1,\"ultimo\":false}]";

        when(httpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn(jsonResponse);

        // When
        List<CalendarDayResponse> result = riksbankenApi.getCalendarDays(date);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("2024-01-01", result.getFirst().calendarDate());
        assertFalse(result.getFirst().swedishBankday());
    }

    @Test
    void getCrossRates_shouldReturnListOfCrossRateResponse() throws Exception {
        // Given
        String seriesId1 = "SEKUSDPMI";
        String seriesId2 = "SEK";
        LocalDate from = LocalDate.of(2024, 1, 1);
        String jsonResponse = "[{\"date\":\"2024-01-01\",\"value\":10.5}]";

        when(httpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn(jsonResponse);

        // When
        List<CrossRateResponse> result = riksbankenApi.getCrossRates(seriesId1, seriesId2, from);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("2024-01-01", result.getFirst().date());
        assertEquals(10.5, result.getFirst().value());
    }

    @Test
    void getCalendarDays_shouldThrowExceptionWhenHttpClientFails() throws Exception {
        // Given
        LocalDate date = LocalDate.of(2024, 1, 1);
        when(httpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenThrow(new RuntimeException("API Error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> riksbankenApi.getCalendarDays(date));
    }
}
