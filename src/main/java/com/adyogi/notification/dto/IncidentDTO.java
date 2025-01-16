package com.adyogi.notification.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import static com.adyogi.notification.utils.constants.RequestDTOConstants.*;

@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class IncidentDTO {

    @JsonProperty(ALERT_ID)
    private String alertId;
    @JsonProperty(CLIENT_ID)
    private String clientId;
    @JsonProperty(METRIC_NAME)
    private String metricName;
    @JsonProperty(OBJECT_TYPE)
    private String objectType;
    @JsonProperty(OBJECT_ID)
    private String objectId;
    @JsonProperty(MESSAGE)
    private String message;
    @JsonProperty(NOTIFICATION_STATUS)
    private String notificationStatus;
    @JsonProperty(INCIDENT_STATUS)
    private String incidentStatus;
    @JsonProperty(STATUS)
    private String status;
    @JsonProperty(BASE_VALUE)
    private String baseValue;
    @JsonProperty(CREATED_AT)
    private String createdAt;
    @JsonProperty(UPDATED_AT)
    private String updatedAt;
    @JsonProperty(VALUE)
    private String value;
    @JsonProperty(VALUE_DATA_TYPE)
    private String valueDataType;
    @JsonProperty(ALERT_RESEND_INTERVAL_MIN)
    private String alertResendIntervalMin;
    @JsonProperty(ALERT_CHANNEL_FIELD_NAME)
    private String alertChannel;
    @JsonProperty(NOTIFICATION_SENT_AT)
    private String notificationSentAT;
    @JsonProperty(INCIDENT_ID)
    private Long incidentId;

}
