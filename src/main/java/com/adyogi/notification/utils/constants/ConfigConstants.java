package com.adyogi.notification.utils.constants;

import java.util.HashMap;
import java.util.Map;

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
    String SENDGRID_API_KEY = "${SEND_GRID_API_KEY}";
    String EMAIL_SUB="email-sub";
    String PAGE_SIZE = "1";
    String PAGE_LIMIT = "100";
    String PAGE = "page";
    String LIMIT = "limit";
    String BULK_SAVE_METRICS_TASK= "bulkSaveMetricsTaskExecutor";
    String SEND_EMAIL_SINGLE_CLIENT = "sendEmailSingleClient";
    String PROCESS_INCIDENT_TASK = "processIncidentTaskExecutor";
    String PROCESS_BULK_INCIDENT_TASK = "processBulkIncidentTaskExecutor";
    String ALL_CLIENT_EMAIL = "allClientEmails";
    String EMAIL_CACHE = "emailCache";
    String GENERATE_EMAIL_SUB_TOPIC = "trigger-email-sub";
    String INCIDENT_COMPUTE_SUB = "incident-compute-sub";
    String ALL_CLIENT = "allClientEmail";
    int MIN_LAST_EMAIL_INTERVAL = 5;
    long ALERT_INTERVAL_MS = 30 * 60 * 60 * 1000;
    int MIN_LAST_INCIDENT_TRIGGER = 60;

    String ALL_CLIENT_INCIDENT = "allClientIncident";

    Map<String, Integer> rateLimits = new HashMap<>(Map.of(
            ALL_CLIENT_EMAIL, MIN_LAST_EMAIL_INTERVAL,
            ALL_CLIENT_INCIDENT, MIN_LAST_INCIDENT_TRIGGER));

    String CLIENT_CACHE = "clientCache";
    String VALIDATE_CLIENT_ID = "validateClientId";
    String CLIENT_ID_KEY = "#clientId";

    String CACHE_ALL_ALERT = "allAlerts";
    String CACHE_ALERT = "alerts";

    String CACHE_NOTIFICATION_CONFIG_MAP = "notificationConfigMap";


    String SENDGRID_ENDPOINT = "mail/send";

    String SENDGRID_EMAIL_FORMAT = "text/html";

    int MAX_CACHE_RATE_LIMITER = 1000;
}
