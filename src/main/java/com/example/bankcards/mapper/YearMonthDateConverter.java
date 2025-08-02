package com.example.bankcards.mapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.sql.Date;
import java.time.YearMonth;

import java.util.Optional;

@Converter(autoApply = true)
public class YearMonthDateConverter implements AttributeConverter<YearMonth, Date> {
    @Override
    public Date convertToDatabaseColumn(YearMonth attribute) {
        return Optional.ofNullable(attribute)
                .map(ym -> Date.valueOf(ym.atDay(1)))
                .orElse(null);
    }

    @Override
    public YearMonth convertToEntityAttribute(Date dbData) {
        return Optional.ofNullable(dbData)
                .map(Date::toLocalDate)
                .map(YearMonth::from)
                .orElse(null);
    }
}
