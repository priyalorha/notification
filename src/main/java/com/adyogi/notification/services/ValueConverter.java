package com.adyogi.notification.services;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.adyogi.notification.database.mongo.entities.Value;
import com.fasterxml.jackson.databind.ObjectMapper;

@Converter
public class ValueConverter implements AttributeConverter<Value, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Value value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting Value to JSON", e);
        }
    }

    @Override
    public Value convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, Value.class); // Jackson will resolve the correct subclass here
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting JSON to Value", e);
        }
    }
}
