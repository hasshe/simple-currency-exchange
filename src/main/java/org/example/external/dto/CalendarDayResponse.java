package org.example.external.dto;

public record CalendarDayResponse(
        String calendarDate,
        boolean swedishBankday,
        int weekYear,
        int weekNumber,
        int quarterNumber,
        boolean ultimo
) {
}
