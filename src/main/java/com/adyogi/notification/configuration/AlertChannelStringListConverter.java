package com.adyogi.notification.configuration;

import com.adyogi.notification.utils.constants.TableConstants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class AlertChannelStringListConverter implements AttributeConverter<List<String>, String> {

    private static final Gson gson = new Gson();

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        return gson.toJson(attribute);  // Converts List<String> to JSON string
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        List<String> stringList =  gson.fromJson(dbData, new TypeToken<List<String>>(){}.getType());
        return stringList.stream()
                .map(channelName -> TableConstants.ALERT_CHANNEL.valueOf(channelName)) // Correct usage
                .collect(Collectors.toList());
    }
}

