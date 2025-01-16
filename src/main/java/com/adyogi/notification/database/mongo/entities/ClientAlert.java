package com.adyogi.notification.database.mongo.entities;


import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.Valid;

import static com.adyogi.notification.utils.constants.MongoConstants.*;


@Document(collection = CLIENT_ALERT_COLLECTION_NAME)
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Valid
public class ClientAlert extends DefaultAlert {

    @Field(name = PARSE_CLIENT_ID)
    private String clientId;

}