package com.adyogi.notification.database.mongo.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.ToString;

import static com.adyogi.notification.utils.constants.MongoConstants.CLIENT_COLLECTION_NAME;
import static com.adyogi.notification.utils.constants.MongoConstants.CLIENT_FLAG_ATTRITED_COLUMN;

@Data
@ToString
@Document(collection = CLIENT_COLLECTION_NAME)
public class ParseClient {
    @Id
    private String id;

    @JsonProperty(CLIENT_FLAG_ATTRITED_COLUMN)
    private String flagAttrited;

//    private String clientId;
}