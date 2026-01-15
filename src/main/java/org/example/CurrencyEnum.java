package org.example;

public enum CurrencyEnum {
    SEK("SEKETT"),
    EUR("SEKEURPMI"),
    USD("SEKUSDPMI");

    private final String currencyCode;

    CurrencyEnum(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }
}
