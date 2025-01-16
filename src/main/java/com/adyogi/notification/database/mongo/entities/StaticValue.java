package com.adyogi.notification.database.mongo.entities;

import com.adyogi.notification.utils.constants.MongoConstants;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.*;

import javax.validation.Valid;

import static com.adyogi.notification.utils.constants.MongoConstants.STATIC;

@Data
@ToString(callSuper = true)
@AllArgsConstructor
@Valid
@EqualsAndHashCode(callSuper = true)
@JsonTypeName(STATIC)
@NoArgsConstructor
public class StaticValue extends Value {
    private String value;

    public StaticValue(String aStatic, boolean compareWithPrevious, String value) {
        this.setType(MongoConstants.ValueType.STATIC); // Set the discriminator
        this.value = value;
        this.setCompareWithPrevious(compareWithPrevious);
    }

    @Override
    public MongoConstants.ValueType getType() {

        return MongoConstants.ValueType.STATIC;
    }
}
