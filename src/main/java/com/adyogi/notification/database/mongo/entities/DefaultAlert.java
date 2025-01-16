package com.adyogi.notification.database.mongo.entities;

import com.adyogi.notification.utils.constants.TableConstants;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.adyogi.notification.utils.constants.MongoConstants.*;

@Document(collection = DEFAULT_ALERT_COLLECTION_NAME)
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)

public class DefaultAlert {

    @Field(name = OBJECT_ID)
    @Id
    private String objectId;

    @Field(name = NAME)
    private String name;

    @Field(name = TRIGGER_CONDITIONS)
    private List<TriggerCondition> triggerConditions = new ArrayList<>();

    @Field(name = STATUS)
    private TableConstants.STATUS status;

    @Field(name = MESSAGE)
    private String message;

    @Field(name = ALERT_RESEND_INTERVAL_MIN)
    private Integer alertResendIntervalMin;

    @Field(name = ALERT_CHANNEL)
    private List<TableConstants.ALERT_CHANNEL> alertChannel;


    @Field(name = CREATED_AT)
    private Date createdAt;

    @Field(name = UPDATED_AT)
    private Date updatedAt;

}
