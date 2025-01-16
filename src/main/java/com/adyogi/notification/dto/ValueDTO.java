package com.adyogi.notification.dto;
import com.adyogi.notification.utils.constants.MongoConstants;
import com.adyogi.notification.utils.constants.RequestDTOConstants;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static com.adyogi.notification.utils.constants.MongoConstants.*;
import static com.adyogi.notification.utils.constants.RequestDTOConstants.COMPARE_WITH_PREVIOUS_ALIAS;
import static com.adyogi.notification.utils.constants.ValidationConstants.VALUE_REQUIRED;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonSubTypes({
        @JsonSubTypes.Type(value = StaticValueDTO.class, name = STATIC),
        @JsonSubTypes.Type(value = StaticIntValueDTO.class, name = STATIC_INT),
        @JsonSubTypes.Type(value = DynamicDayValueDTO.class, name = DATE_DYNAMIC),
        @JsonSubTypes.Type(value = PercentageValueDTO.class, name = PERCENTAGE),
        @JsonSubTypes.Type(value = StaticBooleanValueDTO.class, name = STATIC_BOOLEAN),
})


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = RequestDTOConstants.TYPE,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        visible = true)
@Valid
public class ValueDTO {
    @JsonProperty(TYPE)
    @NotNull(message = VALUE_REQUIRED)
    MongoConstants.ValueType type;

    @JsonProperty(COMPARE_WITH_PREVIOUS)
    @JsonAlias(COMPARE_WITH_PREVIOUS_ALIAS)
    private Boolean compareWithPrevious;
    public Boolean getCompareWithPrevious() {
        return compareWithPrevious != null && compareWithPrevious;
    }

}



