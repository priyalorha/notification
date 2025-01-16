package com.adyogi.notification.utils.constants;

public interface ErrorConstants {
        String INSERT_ERRORS_MESSAGE = "Errors occurred while inserting rows";
        String BATCH_PROCESSING_ERROR = "Error occurred while processing batch at offset %d: %s";
        String CLIENT_ID_MISSING_WARNING = "Field 'parse_client_id' is missing or null in the row: %s";
        String MATCH_RATE_VALUE_WARNING = "Value for match rate '%s' is null or empty for clientId: %s";
        String MATCH_RATE_NOT_FOUND_WARNING = "No value found for match rate '%s' for clientId: %s";
        String METRICS_SENT_ERROR = "Failed to send metrics. Error code: %d";
        String ASYNC_METRICS_SEND_ERROR = "Error sending metrics asynchronously: %s";
        String INVALID_ENUM_VALUES = "Invalid value. Allowed values are: {enumValues}";
        String FAILED_FIELD_ACCESS = "Failed to access field value: %s";

        String MALFORMED_JSON_REQUEST = "Malformed JSON request";
        String INVALID_VALUE_FORMAT = "Invalid value '%s' for field '%s'. Expected type: %s";
        String INVALID_ENUM_VALUE = "Invalid value '%s' for field '%s'. Allowed values are: %s";
        String FIELD_VALIDATION_ERROR = "Validation error for field '%s': %s";

        String DIFFERENT_CLIENT_ID = "Client ID mismatch: Provided: %s, Expected: %s";
        String INVALID_CLIENT_ID = "Invalid Client ID: %s";

        String ALERT_NOT_FOUND_MESSAGE = "Alert with id %s not found for clientId %s";

        String ALERT_NOT_FOUND = "Alert not found with id %s";

        String INCIDENT_NOT_FOUND = "Incident not found";

        String FAILED_TO_READ_FROM_BIGQUERY = "Failed to read from BigQuery";

        String ERROR_INSERTING_BASELINE_TO_BQ = "Error inserting baseline to BigQuery";

        String INCIDENT_RESOLVE_FAILED = "Failed to resolve incident clientId: %s, incidentId: %s";


        String INCIDENT_RESOLVED_SUCCESSFULLY = "Incident resolved successfully.";

        String BASELINE_NOT_FOUND_FOR_INCIDENT = "Baseline not found for clientId: %s, incidentId: %s";

        String ERROR_ENABLING_ALERTS = "Error enabling incident alerts clientId: %s, incidentId: %s";

        String ERROR_PAUSING_ALERTS = "Error pausing incident alerts clientId %s, incidentId %s";

        String ERROR_FETCHING_INCIDENT = "Error fetching incident clientId: %s, incidentId: %s";

        String ALERTS_FOR_INCIDENT_PAUSED_SUCCESSFULLY = "Alerts for the incident paused successfully." ;

        String ALERTS_FOR_INCIDENT_ENABLED_SUCCESSFULLY = "Alerts for the incident enabled successfully.";

        String ERROR_FAILED_API_CALL = "Error during Adyogi CLIENT API call:";


        String ERROR_SAVING_METRIC="Error saving metrics";

        String ERROR_TRIGGER_CONDITION_DTO="Unexpected error converting TriggerConditionDTO to entity";

        String ERROR_CONVERTING_DTO_CONDITION="Error converting TriggerConditionDTO to TriggerCondition";

        String MESSAGE_REQUIRED = "message is required";

        String MISSING_ALERT_NAME =  "missing alert name";

        String TRIGGER_MIN_LENGTH = "atleast one trigger condition is required";
        String TRIGGER_MAX_LENGTH =  "can store only one trigger condition";

        String CONDITION_TYPE_BLANK = "Condition type cannot be blank";
        String MISSING_METRIC_NAME = "Missing metric_name";

        String OPERATOR_CANNOT_NULL = "Operator cannot be null";
        String VALUE_CANNOT_NULL = "Value cannot be null";
        String TRIGGER_CONDITION_REQUIRED = "Trigger condition required";

        String INCIDENT_TRIGGERED_SUCCESSFULLY = "Incident triggered successfully";

        String CHANNEL_ALREADY_EXISTS = "Channel already exists for clientId: %s";
        String CHANNEL_NOT_FOUND = "Channel with id %s not found for clientId %s";

        String CONVERSION_FAILED = "Error converting TriggerConditionDTO to entity";

        String UNSUPPORTED_VALUE_TYPE="Unsupported value type:" ;

        String ERROR_PROCESSING_INCIDENT = "Error triggering incident for client";

}
