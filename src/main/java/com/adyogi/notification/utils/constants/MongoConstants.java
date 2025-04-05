package com.adyogi.notification.utils.constants;

public interface MongoConstants {


    String BACK4APP_PARSE_APPLICATION_ID_HEADER="X-Parse-Application-Id";
    String BACK4APP_PARSE_REST_API_KEY_HEADER="X-Parse-REST-API-Key";
    String BACK4APP_PARSE_SESSION_TOKEN_HEADER="X-Parse-Session-Token";

    String ALERT_CHANNEL_COLLECTION_NAME = "AlertChannel";
    String CLIENT_ALERT_COLLECTION_NAME = "Alert";
    String DEFAULT_ALERT_COLLECTION_NAME = "DefaultAlert";
    String CLIENT_COLLECTION_NAME = "Client";
    String OBJECT_ID = "_id";
    String PARSE_CLIENT_ID = "clientId";
    String OBJECT_IDENTIFIER = "objectIdentifier";
    String UPDATED_AT = "_updated_at";
    String CREATED_AT = "_created_at";
    String COMMUNICATION_CONFIGURATION = "communicationConfiguration";
    String NAME = "name";
    String TRIGGER_CONDITIONS = "triggerConditions";
    String STATUS = "status";
    String MESSAGE = "message";
    String ALERT_CHANNEL = "alertChannel";
    String ALERT_RESEND_INTERVAL_MIN = "alertResendIntervalMin";


    String FROM_EMAIL = "fromEmail";
    String TO_EMAIL = "toEmail";

    enum ValueType {
        STATIC,
        STATIC_INT,
        STATIC_BOOLEAN,
        DATE_DYNAMIC,
        PERCENTAGE,
        STATIC_FLOAT

    }


    String TYPE = "type";
    String VALUE_COL_NAME = "value";
    String COMPARE_WITH_PREVIOUS = "compareWithPrevious";
    String DAY_OFFSET = "day_offset";
    String STATIC = "STATIC";
    String STATIC_INT = "STATIC_INT";
    String DATE_DYNAMIC = "DATE_DYNAMIC";
    String PERCENTAGE = "PERCENTAGE";
    String STATIC_BOOLEAN = "STATIC_BOOLEAN";
    String STATIC_FLOAT = "STATIC_FLOAT";

    String PARSE_CLIENT_OBJECT_IDENTIFIER = "objectIdentifier";

    String VALUE = "VALUE";


    String CLIENT_FLAG_ATTRITED_COLUMN="flag_attrited";

}
