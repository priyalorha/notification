package com.adyogi.notification.utils.constants;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.List;

public interface TableConstants {

    String BIGQUERY_NOTIFICATION_DATASET_NAME = "Notifications";
    String BIGQUERY_METRICS_TABLE_NAME = "metrics";
    String BIGQUERY_BASELINE_TABLE_NAME = "baseline";
    String BIGQUERY_INCIDENTS_TABLE_NAME = "incident";
    String TABLE_NAME_METRICS = "metrics";
    String TABLE_NAME_BASELINE = "baseline";
    String TABLE_NAME_INCIDENTS = "incidents";

    String ID = "id";
    String CLIENT_ID = "client_id";
    String METRIC_COL_NAME = "metric";
    String OBJECT_TYPE_COL_NAME = "object_type";
    String OBJECT_IDENTIFIER = "object_identifier";
    String VALUE = "value";
    String VALUES = "values";
    String VALUE_DATA_TYPE = "value_datatype";
    String CREATED_AT = "created_at";
    String UPDATED_AT = "updated_at";
    String ALERT_RESEND_INTERVAL_MIN = "alert_resend_interval_min";
    String ALERT_CHANNEL = "alert_channel";
    String INCIDENT_ID = "incident_id";

    String BASE_VALUE_INCIDENT_COL = "baseValue";


    String ALERT_ID = "alert_id";
    String BASE_VALUE = "value";
    String BASE_VALUE_DATATYPE = "value_datatype";


    String MESSSAGE = "message";
    String NOTIFICATION_STATUS_COL_NAME = "notification_status";
    String STATUS_COL_NAME = "status";
    String INCIDENT_STATUS_COL_NAME = "incident_status";
    String NOTIFICATION_SENT_AT = "notification_sent_at";

    String PARSE_CLIENT_ID = "parse_client_id";


    enum METRIC {
        INTEGRATION_FAILURE("INTEGRATION_FAILURE"),
        MATCH_RATE("MATCH_RATE"),
        STOPLOSS_LIMIT_REACHED("STOPLOSS_LIMIT_REACHED"),
        STOPLOSS_EXCLUSION_COUNT("STOPLOSS_EXCLUSION_COUNT"),
        STOPLOSS_EXCLUSION_DATE("STOPLOSS_EXCLUSION_DATE"),
        PRODUCT_SET_COUNT("PRODUCT_SET_COUNT"),
        STOPLOSS_RAN_DATE("STOPLOSS_RAN_DATE");


        String metric;
        METRIC(String metric) {
            this.metric = metric;
        }

    }

    enum OBJECT_TYPE {
        CLIENT_ID("CLIENT_ID"),
        SEGEMENT_ID("SEGMENT_ID"),
        PRODUCT_SET_ID("PRODUCT_SET_ID"),
        RULE_ID("RULE_ID"),
        CATALOG_TRACKING_MATCH_RATE("CATALOG_TRACKING_MATCH_RATE"),
        CATALOG_FB_MATCH_RATE("CATALOG_FB_MATCH_RATE"),
        CATALOG_GOOGLE_MATCH_RATE("CATALOG_GOOGLE_MATCH_RATE"),
        CATALOG_WEBSITE_ORDER_MATCH_RATE("CATALOG_WEBSITE_ORDER_MATCH_RATE");
        String objectType;

        OBJECT_TYPE(String objectType) {
            this.objectType = objectType;}

    }

    enum INCIDENT_STATUS {
        OPEN,
        RESOLVED
    }

    enum NOTIFICATION_STATUS {
        SENT,
        PENDING,
        FAILED
    }

    enum ALERT_STATUS {
        ENABLED,
        DISABLED,

    }

    enum DURATION {
        DAY,
        WEEK,
        MONTH
    }

    enum ALERT_CHANNEL {
        ALL,
        EMAIL,
        SLACK,
    }

    String CATALOG_TRACKING_MATCH_RATE = "catalog_tracking_match_rate";
    String CATALOG_FB_MATCH_RATE = "catalog_fb_match_rate";
    String CATALOG_GOOGLE_MATCH_RATE = "catalog_google_match_rate";
    String CATALOG_WEBSITE_ORDER_MATCH_RATE = "catalog_website_order_match_rate";


    List<String> MATCH_RATE_LIST = List.of(
            CATALOG_TRACKING_MATCH_RATE,
            CATALOG_FB_MATCH_RATE,
            CATALOG_GOOGLE_MATCH_RATE,
            CATALOG_WEBSITE_ORDER_MATCH_RATE);


    String INSERT_METRICS_SQL_RAW_QUERY = "INSERT INTO metrics (client_id, metric, value, value_datatype, created_at, updated_at, " +
            "object_type, object_identifier) VALUES ";

    String INSERT_METRICS_SQL_RAW_QUERY_DUPLICATE_KEY_UPDATE_SQL =
            "ON DUPLICATE KEY UPDATE " +
                    "value = VALUES(value), " +
                    "created_at = VALUES(created_at), " +
                    "updated_at = VALUES(updated_at)";


    enum ValueType {
        STATIC,
        STATIC_INT,
        DATE_DYNAMIC;
    }

    @Getter
    enum Operator {

        EQUALS("eq"),
        NOT_EQUALS("notEquals"),
        CONTAINS("contains"),
        NOT_CONTAINS("notContains"),
        GREATER_THAN_EQUAL_TO("gte"),
        LESS_THAN_EQUAL_TO("lte"),
        GREATER_THAN("gt"),
        LESS_THAN("lt"),
        DELTA("delta");
        String operatorSign;
        Operator(String operatorSign) {
            this.operatorSign = operatorSign;
        }

    }



}