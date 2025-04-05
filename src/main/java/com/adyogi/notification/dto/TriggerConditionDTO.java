package com.adyogi.notification.dto;


import com.adyogi.notification.utils.constants.RequestDTOConstants;
import com.adyogi.notification.utils.constants.TableConstants;
import com.adyogi.notification.validators.OnCreate;
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
    @JsonProperty(RequestDTOConstants.METRIC)
    @NotNull(groups = OnCreate.class,message = MISSING_TRIGGER_CONDITION_METRIC)
    private TableConstants.METRIC metric;

    @JsonProperty(RequestDTOConstants.OPERATOR)
    @NotNull(groups = OnCreate.class,message = OPERATOR_CANNOT_NULL)
    private TableConstants.Operator operator;

    @NotNull(groups = OnCreate.class,message = VALUE_CANNOT_NULL)
    @Valid
    private ValueDTO value;
}