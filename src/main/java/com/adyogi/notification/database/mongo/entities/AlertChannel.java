package com.adyogi.notification.database.mongo.entities;

import com.adyogi.notification.utils.constants.TableConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.adyogi.notification.utils.constants.MongoConstants.*;

@Document(collection = ALERT_CHANNEL_COLLECTION_NAME)
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AlertChannel {

    @Id
    @Field(name = OBJECT_ID)
    private String objectId;

    @Field(PARSE_CLIENT_ID)
    private String clientId;

    @Field(name = ALERT_CHANNEL)
    private TableConstants.ALERT_CHANNEL alertChannel;

    @Field(name = COMMUNICATION_CONFIGURATION)
    private CommunicationConfiguration communicationConfiguration;

    @Field(name = CREATED_AT)
    private Date createdAt;

    @Field(name = UPDATED_AT)
    private Date updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class CommunicationConfiguration {

        @Field(name = FROM_EMAIL)
        private String fromEmail;

        @Field(name = TO_EMAIL)
        private List<String> toEmail = new ArrayList<>(); // Initialized to avoid NullPointerException
    }
}