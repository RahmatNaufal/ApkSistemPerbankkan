package com.intbank.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.math.BigDecimal;

/** Menyimpan value object {@link Money} sebagai DECIMAL(19,0) di database. */
@Converter(autoApply = true)
public class MoneyConverter implements AttributeConverter<Money, BigDecimal> {

    @Override
    public BigDecimal convertToDatabaseColumn(Money money) {
        return money == null ? null : money.toBigDecimal();
    }

    @Override
    public Money convertToEntityAttribute(BigDecimal value) {
        return value == null ? null : Money.of(value);
    }
}
