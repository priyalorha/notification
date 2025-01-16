package com.adyogi.notification.utils.constants;

import javax.validation.constraints.Pattern;

public interface ValidationConstants {

    String MISSING_CLIENT_ID = "client_id is required";
    String MISSING_METRICS_NAME = "metric_name is required";
    String MISSING_OBJECT_TYPE = "object_type is required";
    String MISSING_OBJECT_ID = "object_id is required";
    String MISSING_VALUE = "value is required";
    String MISSING_VALUE_DATA_TYPE = "value_datatype is required";
    String MISSING_STATUS = "status is required";
    String MISSING_ALERT_CHANNEL = "alert_channel is required";
    String MISSING_COMMUNICATION_CONFIGURATION = "Communication Channel Configuration is required";
    String EMAIL_REGEX = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}";
    static final Pattern.Flag[] EMAIL_REGEX_FLAGS = {Pattern.Flag.CASE_INSENSITIVE};

    // Validation Messages
    String MISSING_FROM_EMAIL = "fromEmail cannot be blank";
    String INVALID_FROM_EMAIL_FORMAT = "Invalid email format for fromEmail";
    String MISSING_TO_EMAIL_LIST = "toEmail list cannot be empty";
    String INVALID_TO_EMAIL_FORMAT = "Invalid email format in toEmail list";
    String TO_EMAIL_LIST_SIZE_EXCEEDED = "toEmail list cannot have more than 50 email addresses";

    String TO_EMAIL_LIST_ERROR = "toEmail must be a list";


    String MUST_BE_LIST = "Field must be a list";

    // Constraints
    int TO_EMAIL_MAX_SIZE = 50;

    String TRIGGER_CONDITION_MIN = "trigger_conditions.value.value must be at least 0";
    String TRIGGER_CONDITION_DATE_OFFSET_REQUIRED = "trigger_conditions.value.date_offset is required";
    String TRIGGER_CONDITION_PERCENTATE_REQUIRED = "trigger_conditions.value.percentage is required";
    String TRIGGER_CONDITION_VALUE_REQUIRED = "trigger_conditions.value.value is required and cannot be blank.";
    String VALUE_REQUIRED = "value.type is required";
}
