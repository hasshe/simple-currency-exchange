package org.example.mapper;

import org.example.controller.dto.responses.ExchangeRateResponse;
import org.example.repository.entities.ExchangeRate;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ExchangeRateMapper {
    ExchangeRateResponse toDto(ExchangeRate exchangeRate);
}