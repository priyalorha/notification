package com.adyogi.notification.database.mongo.entities;

import com.adyogi.notification.utils.constants.MongoConstants;
import lombok.*;
import org.springframework.data.annotation.TypeAlias;

import static com.adyogi.notification.utils.constants.MongoConstants.DATE_DYNAMIC;

@Data
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TypeAlias(DATE_DYNAMIC)
public class DynamicDay extends Value {
    private int dayOffset;
    @Override
    public MongoConstants.ValueType getType() {
        return MongoConstants.ValueType.DATE_DYNAMIC;
    }
}