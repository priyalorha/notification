package com.adyogi.notification.utils;

import com.google.cloud.bigquery.InsertAllRequest;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static com.adyogi.notification.utils.constants.ErrorConstants.FAILED_FIELD_ACCESS;

public class Utils {

    public static InsertAllRequest.RowToInsert convertToRowToInsert(String rowId, Object obj) {
        Map<String, Object> map = convertToMap(obj); // Convert object to Map
        return InsertAllRequest.RowToInsert.of(rowId, map);
    }

    public static Map<String, Object> convertToMap(Object obj) {
        Map<String, Object> result = new HashMap<>();
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                result.put(field.getName(), field.get(obj));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(String.format(FAILED_FIELD_ACCESS,field.getName()), e);
            }
        }
        return result;
    }
}
