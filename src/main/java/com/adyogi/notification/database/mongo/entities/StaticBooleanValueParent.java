package com.adyogi.notification.database.mongo.entities;

import com.adyogi.notification.utils.constants.MongoConstants;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Field;

import static com.adyogi.notification.utils.constants.MongoConstants.STATIC_BOOLEAN;
import static com.adyogi.notification.utils.constants.MongoConstants.VALUE_COL_NAME;

@Data
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeName(STATIC_BOOLEAN)
public class StaticBooleanValueParent extends Value {
    private boolean value;

    @Override
    public MongoConstants.ValueType getType() {
        return MongoConstants.ValueType.STATIC_BOOLEAN;
    }
}
