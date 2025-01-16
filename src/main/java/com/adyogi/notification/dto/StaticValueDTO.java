package com.adyogi.notification.dto;


import com.adyogi.notification.utils.constants.MongoConstants;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static com.adyogi.notification.utils.constants.MongoConstants.VALUE_COL_NAME;
import static com.adyogi.notification.utils.constants.ValidationConstants.TRIGGER_CONDITION_VALUE_REQUIRED;

@Data
@ToString(callSuper = true)
@Valid
@NoArgsConstructor
@AllArgsConstructor
public class StaticValueDTO extends ValueDTO {

    @JsonProperty( VALUE_COL_NAME)
    String value;
    @NotNull(message = TRIGGER_CONDITION_VALUE_REQUIRED)

    @Override // This annotation is optional here but clarifies intent
    public MongoConstants.ValueType getType() {
        return MongoConstants.ValueType.STATIC;
    }
}
