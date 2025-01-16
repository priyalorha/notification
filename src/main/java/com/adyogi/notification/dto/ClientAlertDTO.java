package com.adyogi.notification.dto;

import com.adyogi.notification.utils.constants.RequestDTOConstants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientAlertDTO extends DefaultAlertDTO {

    @JsonProperty(RequestDTOConstants.CLIENT_ID)
    private String clientId;
}