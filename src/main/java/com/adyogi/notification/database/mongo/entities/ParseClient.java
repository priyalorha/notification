package com.adyogi.notification.database.mongo.entities;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.ToString;

import static com.adyogi.notification.utils.constants.MongoConstants.CLIENT_COLLECTION_NAME;

@Data
@ToString
@Document(collection = CLIENT_COLLECTION_NAME)
public class ParseClient {
    @Id
    private String clientId;
//    private String clientId;
}