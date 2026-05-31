package com.reeya.payment_risk_engine.model.functions;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Converts list of strings to a single string separated by semicolon and vice versa.
 * Improvement for the service would remove this as the table would store reasons in a child table with FK constraint
 */
@Component
@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        return attribute == null ? null : String.join(";", attribute);
    }

    @Override
    public List<String> convertToEntityAttribute(String data) {
        return data == null
                ? List.of()
                : Arrays.asList(data.split(";"));
    }
}
