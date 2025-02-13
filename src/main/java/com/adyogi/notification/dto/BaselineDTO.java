package com.adyogi.notification.dto;

import com.adyogi.notification.utils.constants.BigQueryConstants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@ToString
public class BaselineDTO {

    @JsonProperty(BigQueryConstants.ALERT_ID)
    String alertId;

    @JsonProperty(BigQueryConstants.CLIENT_ID)
    String clientId;

    @JsonProperty(BigQueryConstants.METRIC)
    String metric;

    @JsonProperty(BigQueryConstants.OBJECT_TYPE)
    String objectType;
    @JsonProperty(BigQueryConstants.OBJECT_IDENTIFIER)
    String objectIdentifier;

    @JsonProperty(BigQueryConstants.VALUE)
    String value;
    @JsonProperty(BigQueryConstants.VALUE_DATATYPE)
    String valueDataType;
    @JsonProperty(BigQueryConstants.CREATED_AT)
    String createdAt;
    @JsonProperty(BigQueryConstants.UPDATED_AT)
    String updatedAt;

    public Map<String, Object> toMap() { // Use Object as value type
        Map<String, Object> map = new HashMap<>();
        map.put(BigQueryConstants.ALERT_ID, this.alertId);
        map.put(BigQueryConstants.CLIENT_ID, this.clientId);
        map.put(BigQueryConstants.METRIC, this.metric);
        map.put(BigQueryConstants.OBJECT_TYPE, this.objectType);
        map.put(BigQueryConstants.OBJECT_IDENTIFIER, this.objectIdentifier);
        map.put(BigQueryConstants.VALUE, this.value);
        map.put(BigQueryConstants.VALUE_DATATYPE, this.valueDataType);
        map.put(BigQueryConstants.CREATED_AT, this.createdAt);
        map.put(BigQueryConstants.UPDATED_AT, this.updatedAt);
        return map;
    }
}
