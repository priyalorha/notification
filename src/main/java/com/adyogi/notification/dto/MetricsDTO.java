package com.adyogi.notification.dto;


import com.adyogi.notification.utils.constants.RequestDTOConstants;
import com.adyogi.notification.utils.constants.TableConstants;
import com.adyogi.notification.utils.constants.TableConstants.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.time.LocalDateTime;
import static com.adyogi.notification.utils.constants.ValidationConstants.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor // Include NoArgsConstructor for deserialization (optional)
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class MetricsDTO {

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String id;


    @NotNull(message = MISSING_CLIENT_ID)
    @JsonProperty(RequestDTOConstants.CLIENT_ID)
    private String clientId;

    @NotNull(message = MISSING_METRICS_NAME)
    @JsonProperty(RequestDTOConstants.METRIC_NAME)
    private TableConstants.METRIC_NAME metricName;

    @NotNull(message = MISSING_OBJECT_TYPE)
    @JsonProperty(RequestDTOConstants.OBJECT_TYPE)

    private TableConstants.OBJECT_TYPE objectType;

    @NotNull(message = MISSING_OBJECT_ID)
    @JsonProperty(RequestDTOConstants.OBJECT_ID)
    private String objectId;

    @NotBlank(message = MISSING_VALUE)
    @JsonProperty(RequestDTOConstants.VALUE)
    private String value;

    @NotBlank(message = MISSING_VALUE_DATA_TYPE)
    @JsonProperty(RequestDTOConstants.VALUE_DATA_TYPE)
    private String valueDataType;

    @JsonProperty(RequestDTOConstants.CREATED_AT)
    private LocalDateTime createdAt; // Use Java's LocalDateTime

    @JsonProperty(RequestDTOConstants.UPDATED_AT)
    private LocalDateTime updatedAt;


    public MetricsDTO(String clientId,
                      String metricName,
                      String objectType,
                      String objectId,
                      String value,
                      String valueDataType) {

        this.clientId = clientId;
        this.metricName = METRIC_NAME.valueOf(metricName);
        this.objectType = OBJECT_TYPE.valueOf(objectType.toUpperCase());
        this.objectId = objectId;
        this.value = value;
        this.valueDataType = valueDataType;
    }
}