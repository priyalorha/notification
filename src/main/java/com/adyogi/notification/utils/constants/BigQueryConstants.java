package com.adyogi.notification.utils.constants;

public interface BigQueryConstants {

    String MATCH_RATE_QUERY = "SELECT * FROM `stellar-display-145814.table_output.product_performance_match_rate_table`";

    int BATCH_SIZE = 100;

    String ID = "ID";
    String INCIDENT_ID = "incident_id";
    String CLIENT_ID = "client_id";
    String METRIC_NAME = "metric_name";
    String OBJECT_TYPE = "object_type";
    String OBJECT_ID = "object_id";
    String VALUE = "value";
    String VALUE_DATATYPE = "value_datatype";
    String CREATED_AT = "created_at";
    String UPDATED_AT = "updated_at";
    String ALERT_ID = "alert_id";
    String MESSSAGE = "message";
    String NOTIFICATION_STATUS = "notification_status";
    String BASE_VALUE = "base_value";
    String INCIDENT_STATUS = "incident_status";
    String STATUS = "status";
    String NOTIFICATION_SENT_AT = "notification_sent_at";
    String ALERT_RESEND_INTERVAL_MIN = "alert_resend_interval_min";


    String INCIDENT_QUERY =
            "SELECT * FROM `stellar-display-145814.Notifications.incident` " +
                    "WHERE client_id = '%s' " +
                    "AND created_at >= TIMESTAMP(DATE_SUB(CURRENT_DATE(), INTERVAL 3 MONTH))";

}
