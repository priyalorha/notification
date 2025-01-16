package com.adyogi.notification.utils.constants;

public interface ConfigConstants {

    String PRODUCTION_ENVIRONMENT="production";
    String DEVELOPMENT_ENVIRONMENT="development";
    String DEFAULT_LOGGING_FIELD_NAME="default";
    String CLIENT_ID_LOGGING_FIELD_NAME="client_id";
    String BULK_METRICS_LOGGING_FIELD_NAME="bulk_metrics";
    String MONGODB_NAME_BACK4APP_ADYOGI_ADS ="mongodb.back4app";
    String ADYOGI_ADS_MONGO_DB_FACTORY = "adyogiAdsMongoDBFactory";
    String ADYOGI_ADS_MONGO_CLIENT ="adyogiAdsMongoClient";
    String BACK4APP_MONGO_TEMPLATE = "back4appMongoTemplate";
    String BACK4APP_MONGODB_FACTORY = "back4appMongoDBFactory";
    String BACK4APP_MONGO_CLIENT ="back4appMongoClient";
    String BACK4APP_PROPERTIES="back4appProperties";
    String SENDGRID_API_KEY = "SEND_GRID_API_KEY";
    String SENDGRID_KEY = System.getenv(SENDGRID_API_KEY);
    String EMAIL_SUB="email-sub";
    String PAGE_SIZE = "50";
    String PAGE_LIMIT = "100";
    String PAGE = "page";
    String LIMIT = "limit";
}
