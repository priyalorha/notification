package com.adyogi.notification.dto;

import com.adyogi.notification.utils.constants.MongoConstants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotNull;

import static com.adyogi.notification.utils.constants.RequestDTOConstants.PERCENTAGE;
import static com.adyogi.notification.utils.constants.TableConstants.VALUE;

@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor

public class StaticBooleanValueDTO extends ValueDTO{
    @JsonProperty( VALUE)
    private boolean value;
    @Override // This annotation is optional here but clarifies intent
    public MongoConstants.ValueType getType() {
        return MongoConstants.ValueType.STATIC_BOOLEAN;
    }
}