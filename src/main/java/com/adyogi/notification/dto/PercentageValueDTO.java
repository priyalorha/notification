package com.adyogi.notification.dto;

import com.adyogi.notification.utils.constants.MongoConstants;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotNull;

import static com.adyogi.notification.utils.constants.RequestDTOConstants.PERCENTAGE;
import static com.adyogi.notification.utils.constants.ValidationConstants.TRIGGER_CONDITION_PERCENTATE_REQUIRED;


@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PercentageValueDTO extends ValueDTO{
    @JsonProperty( PERCENTAGE)
    @NotNull(message = TRIGGER_CONDITION_PERCENTATE_REQUIRED)
    private float percentage;
    @Override // This annotation is optional here but clarifies intent
    public MongoConstants.ValueType getType() {
        return MongoConstants.ValueType.PERCENTAGE;
    }

    public PercentageValueDTO(String aStatic, boolean compareWithPrevious, float value) {
        this.setType(MongoConstants.ValueType.PERCENTAGE); // Set the discriminator
        this.percentage = value;
        this.setCompareWithPrevious(compareWithPrevious);
    }
}