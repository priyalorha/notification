package com.adyogi.notification.database.mongo.entities;
import com.adyogi.notification.utils.constants.MongoConstants;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import lombok.*;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Field;

import static com.adyogi.notification.utils.constants.MongoConstants.*;

@Data
@NoArgsConstructor
@JsonSubTypes({
        @JsonSubTypes.Type(value = StaticValue.class, name=STATIC),
        @JsonSubTypes.Type(value = StaticIntValue.class, name=STATIC_INT),
        @JsonSubTypes.Type(value = DynamicDay.class, name = DATE_DYNAMIC),
        @JsonSubTypes.Type(value = PercentageValue.class, name = PERCENTAGE),
        @JsonSubTypes.Type(value = StaticBooleanValueParent.class, name = STATIC_BOOLEAN),
})
@EqualsAndHashCode
@ToString
@TypeAlias(VALUE)
public abstract class Value {

    @Getter
    @Setter
    @Field(name = MongoConstants.TYPE)
    private MongoConstants.ValueType type;

    @Field(name = MongoConstants.COMPARE_WITH_PREVIOUS)
    private boolean compareWithPrevious;

    public Value(MongoConstants.ValueType type, boolean compareWithPrevious) {
        this.type = type;
        this.compareWithPrevious = compareWithPrevious;
    }
}