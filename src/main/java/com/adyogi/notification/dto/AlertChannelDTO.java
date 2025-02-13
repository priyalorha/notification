package com.adyogi.notification.dto;

import com.adyogi.notification.utils.constants.RequestDTOConstants;
import com.adyogi.notification.utils.constants.TableConstants;
import com.adyogi.notification.validators.EnsureList;
import com.adyogi.notification.validators.OnCreate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.adyogi.notification.utils.constants.ValidationConstants.*;
import static com.adyogi.notification.utils.constants.ValidationConstants.INVALID_TO_EMAIL_FORMAT;


@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlertChannelDTO {

    @JsonProperty(value = RequestDTOConstants.OBJECT_ID)
    private String objectId;

    @JsonProperty(RequestDTOConstants.CLIENT_ID)
    private String clientId;

    @NotNull(groups = OnCreate.class, message = MISSING_ALERT_CHANNEL)
    @JsonProperty(RequestDTOConstants.ALERT_CHANNEL_FIELD_NAME)
    private TableConstants.ALERT_CHANNEL alertChannel;

    @Valid
    @NotNull(groups = OnCreate.class, message = MISSING_COMMUNICATION_CONFIGURATION)
    @JsonProperty(RequestDTOConstants.COMMUNICATION_CONFIGURATION_FIELD_NAME)
    private CommunicationConfiguration communicationConfiguration;

    @JsonProperty(RequestDTOConstants.CREATED_AT)
    private Date createdAt;

    @JsonProperty(RequestDTOConstants.UPDATED_AT)
    private Date updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @Valid
    public static class CommunicationConfiguration {

        @NotNull(groups = OnCreate.class, message = MISSING_FROM_EMAIL)
        @Email(
                regexp = EMAIL_REGEX,
                flags = Pattern.Flag.CASE_INSENSITIVE,
                message = INVALID_FROM_EMAIL_FORMAT
        )
        @JsonProperty(RequestDTOConstants.FROM_EMAIL)
        private String fromEmail;

        @NotEmpty(groups = OnCreate.class, message = MISSING_TO_EMAIL_LIST)
        @Valid // Ensures validation is applied to each element of the list
        @JsonProperty(RequestDTOConstants.TO_EMAIL)
        @Size(max = TO_EMAIL_MAX_SIZE, message = TO_EMAIL_LIST_SIZE_EXCEEDED)

        @EnsureList(message = TO_EMAIL_LIST_ERROR)
        private List<
                @Email(
                        regexp = EMAIL_REGEX,
                        flags = Pattern.Flag.CASE_INSENSITIVE,
                        message = INVALID_TO_EMAIL_FORMAT
                )
                        String
                > toEmail = new ArrayList<>();
    }
}
