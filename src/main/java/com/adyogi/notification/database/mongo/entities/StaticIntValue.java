package com.adyogi.notification.database.mongo.entities;

import com.adyogi.notification.utils.constants.MongoConstants;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;

import static com.adyogi.notification.utils.constants.MongoConstants.STATIC_INT;
import static com.adyogi.notification.utils.constants.MongoConstants.VALUE_COL_NAME;

@Data
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName(STATIC_INT)
public class StaticIntValue extends Value {
    private int value;

    @Override
    public MongoConstants.ValueType getType() {
        return MongoConstants.ValueType.STATIC_INT;
    }
}
