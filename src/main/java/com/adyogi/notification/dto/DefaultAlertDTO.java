package com.adyogi.notification.dto;

import com.adyogi.notification.utils.constants.RequestDTOConstants;
import com.adyogi.notification.utils.constants.TableConstants;
import com.adyogi.notification.validators.EnsureList;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

import static com.adyogi.notification.utils.constants.ErrorConstants.*;
import static com.adyogi.notification.utils.constants.ValidationConstants.MISSING_ALERT_CHANNEL;
import static com.adyogi.notification.utils.constants.ValidationConstants.MISSING_STATUS;


@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Valid
public class DefaultAlertDTO {


    @JsonProperty(RequestDTOConstants.OBJECT_ID)
    private String objectId;

    @JsonProperty(RequestDTOConstants.NAME)
    @NotBlank(message = MISSING_ALERT_NAME)
    private String name;

    @Size(min=1, message = TRIGGER_MIN_LENGTH)
    @Size(max=1, message = TRIGGER_MAX_LENGTH)
    @Valid
    @JsonProperty(RequestDTOConstants.TRIGGER_CONDITIONS)
    @EnsureList
    @NotNull(message = TRIGGER_CONDITION_REQUIRED)
    private List<TriggerConditionDTO> triggerConditions;

    @JsonProperty(RequestDTOConstants.STATUS)
    @NotNull(message = MISSING_STATUS)
    private TableConstants.STATUS status;

    @JsonProperty(RequestDTOConstants.MESSAGE)
    @NotBlank(message = MESSAGE_REQUIRED)
    private String message;


    @JsonProperty(RequestDTOConstants.ALERT_RESEND_INTERVAL_MIN)
    private Integer alertResendIntervalMin;


    @NotEmpty(message = MISSING_ALERT_CHANNEL)
    @JsonProperty(RequestDTOConstants.ALERT_CHANNEL_FIELD_NAME)
    private List<TableConstants.ALERT_CHANNEL> alertChannel;

    private Date createdAt;
    private Date updatedAt;

}
