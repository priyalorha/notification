package com.adyogi.notification.dto;


import com.adyogi.notification.utils.constants.RequestDTOConstants;
import com.adyogi.notification.utils.constants.TableConstants;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static com.adyogi.notification.utils.constants.ErrorConstants.*;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class TriggerConditionDTO {
    @JsonProperty(RequestDTOConstants.METRIC_NAME)
    @NotNull(message = MISSING_METRIC_NAME)
    private TableConstants.METRIC_NAME metricName;

    @JsonProperty(RequestDTOConstants.OPERATOR)
    @NotNull(message = OPERATOR_CANNOT_NULL)
    private TableConstants.Operator operator;

    @NotNull(message = VALUE_CANNOT_NULL)
    @Valid
    private ValueDTO value;
}