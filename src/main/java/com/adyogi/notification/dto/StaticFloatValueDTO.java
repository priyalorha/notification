package com.adyogi.notification.dto;

import com.adyogi.notification.utils.constants.MongoConstants;
import com.adyogi.notification.validators.OnCreate;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static com.adyogi.notification.utils.constants.MongoConstants.VALUE_COL_NAME;
import static com.adyogi.notification.utils.constants.ValidationConstants.TRIGGER_CONDITION_VALUE_REQUIRED;

@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Valid
public class StaticFloatValueDTO extends ValueDTO {
    @JsonProperty(VALUE_COL_NAME)
    @NotNull(groups = OnCreate.class,message = TRIGGER_CONDITION_VALUE_REQUIRED)
    float value;
    @Override // This annotation is optional here but clarifies intent
    public MongoConstants.ValueType getType() {
        return MongoConstants.ValueType.STATIC_FLOAT;
    }
}
