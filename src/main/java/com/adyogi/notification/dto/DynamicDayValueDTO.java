package com.adyogi.notification.dto;

import com.adyogi.notification.utils.constants.MongoConstants;
import com.adyogi.notification.validators.OnCreate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.*;
import static com.adyogi.notification.utils.constants.MongoConstants.DAY_OFFSET;
import static com.adyogi.notification.utils.constants.ValidationConstants.TRIGGER_CONDITION_DATE_OFFSET_REQUIRED;
import static com.adyogi.notification.utils.constants.ValidationConstants.TRIGGER_CONDITION_MIN;

@Data
@ToString(callSuper = true)
@Valid
@NoArgsConstructor
@AllArgsConstructor
public class DynamicDayValueDTO extends ValueDTO {
    @JsonProperty(DAY_OFFSET)
    @Min(groups = OnCreate.class, value = 0, message = TRIGGER_CONDITION_MIN)
    @NotNull(groups = OnCreate.class, message = TRIGGER_CONDITION_DATE_OFFSET_REQUIRED)
    private Integer dayOffset;


    @Override // This annotation is optional here but clarifies intent
    public MongoConstants.ValueType getType() {
        return MongoConstants.ValueType.DATE_DYNAMIC;
    }
}
