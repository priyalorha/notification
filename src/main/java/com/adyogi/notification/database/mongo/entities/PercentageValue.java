package com.adyogi.notification.database.mongo.entities;

import com.adyogi.notification.utils.constants.MongoConstants;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;

import static com.adyogi.notification.utils.constants.MongoConstants.*;


@Data
@ToString(callSuper = true)
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Document
public class PercentageValue extends Value {

    @NotNull
    private float percentage;

    @JsonCreator
    public PercentageValue(@JsonProperty(TYPE) String type,
                           @JsonProperty(PERCENTAGE) float percentage,
                           @JsonProperty(COMPARE_WITH_PREVIOUS) boolean compareWithPrevious) {
        super(MongoConstants.ValueType.PERCENTAGE, compareWithPrevious);  // Fix here
        this.percentage = percentage;
    }
}
