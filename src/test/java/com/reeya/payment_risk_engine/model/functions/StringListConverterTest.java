package com.reeya.payment_risk_engine.model.functions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class StringListConverterTest {

    private StringListConverter converter;

    @BeforeEach
    void setUp()
    {
        converter = new StringListConverter();
    }

    @Test
    void convertToDatabaseColumn_whenAttributeHasValues()
    {
        String result = converter.convertToDatabaseColumn(List.of("Low risk", "IP mismatch"));

        assertEquals("Low risk;IP mismatch", result);
    }

    @Test
    void convertToDatabaseColumn_whenAttributeIsNull()
    {
        String result = converter.convertToDatabaseColumn(null);

        assertNull(result);
    }

    @Test
    void convertToEntityAttribute_whenDatabaseValueHasValues()
    {
        List<String> result = converter.convertToEntityAttribute("Low risk;IP mismatch");

        assertEquals(List.of("Low risk", "IP mismatch"), result);
    }

    @Test
    void convertToEntityAttribute_whenDatabaseValueIsNull()
    {
        List<String> result = converter.convertToEntityAttribute(null);

        assertEquals(List.of(), result);
    }
}
