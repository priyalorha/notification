package com.adyogi.notification.dto;


import com.adyogi.notification.utils.constants.BigQueryConstants;
import com.adyogi.notification.utils.constants.RequestDTOConstants;
import com.adyogi.notification.utils.constants.TableConstants;
import com.adyogi.notification.utils.constants.TableConstants.*;
import com.adyogi.notification.validators.OnCreate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.adyogi.notification.utils.constants.ValidationConstants.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor // Include NoArgsConstructor for deserialization (optional)
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString

public class MetricsDTO {

    @NotNull(groups = OnCreate.class, message = MISSING_CLIENT_ID)
    @JsonProperty(RequestDTOConstants.CLIENT_ID)
    private String clientId;

    @NotNull(groups = OnCreate.class, message = MISSING_METRICS_NAME)
    @JsonProperty(RequestDTOConstants.METRIC)
    private TableConstants.METRIC metric;

    @NotNull(groups = OnCreate.class, message = MISSING_OBJECT_TYPE)
    @JsonProperty(RequestDTOConstants.OBJECT_TYPE)

    private TableConstants.OBJECT_TYPE objectType;

    @NotNull(groups = OnCreate.class, message = MISSING_OBJECT_IDENTIFIER)
    @JsonProperty(RequestDTOConstants.OBJECT_IDENTIFIER)
    private String objectIdentifier;

    @NotBlank(groups = OnCreate.class, message = MISSING_VALUE)
    @JsonProperty(RequestDTOConstants.VALUE)
    private String value;

    @NotBlank(groups = OnCreate.class, message = MISSING_VALUE_DATA_TYPE)
    @JsonProperty(RequestDTOConstants.VALUE_DATA_TYPE)
    private String valueDataType;

    @JsonProperty(RequestDTOConstants.CREATED_AT)
    private LocalDateTime createdAt; // Use Java's LocalDateTime

    @JsonProperty(RequestDTOConstants.UPDATED_AT)
    private LocalDateTime updatedAt;


    public MetricsDTO(String clientId,
                      String metric,
                      String objectType,
                      String objectIdentifier,
                      String value,
                      String valueDataType) {

        this.clientId = clientId;
        this.metric = METRIC.valueOf(metric);
        this.objectType = OBJECT_TYPE.valueOf(objectType.toUpperCase());
        this.objectIdentifier = objectIdentifier;
        this.value = value;
        this.valueDataType = valueDataType;
    }

    public Map<String, Object> toMap() { // Use Object as value type
        Map<String, Object> map = new HashMap<>();
        map.put(BigQueryConstants.CLIENT_ID, this.clientId);
        map.put(BigQueryConstants.METRIC, this.metric.toString());
        map.put(BigQueryConstants.OBJECT_TYPE, this.objectType.toString());
        map.put(BigQueryConstants.OBJECT_IDENTIFIER, this.objectIdentifier);
        map.put(BigQueryConstants.VALUE, this.value);
        map.put(BigQueryConstants.VALUE_DATATYPE, this.valueDataType);
        map.put(BigQueryConstants.CREATED_AT, this.createdAt.toString());
        map.put(BigQueryConstants.UPDATED_AT, this.updatedAt.toString());
        return map;

    }
}