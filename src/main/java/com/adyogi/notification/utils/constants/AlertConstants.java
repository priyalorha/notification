package com.adyogi.notification.utils.constants;

public interface AlertConstants {

    // Metric Names
    String STOPLOSS_LIMIT_REACHED = "STOPLOSS_LIMIT_REACHED";
    String STOP_LOSS_EXCLUSION_COUNT = "STOP_LOSS_EXCLUSION_COUNT";
    String STOPLOSS_INACTIVITY = "STOPLOSS_INACTIVITY";
    String STOPLOSS_EXCLUSION_DATE = "STOPLOSS_EXCLUSION_DATE";
    String PRODUCT_SET_COUNT = "PRODUCT_SET_COUNT";
    String INTEGRATION_FAILURE = "INTEGRATION_FAILURE";
    String MATCH_RATE = "MATCH_RATE";

    // Titles
    String STOPLOSS_LIMIT_REACHED_TITLE = "Stoploss Limit Reached";
    String STOPLOSS_INACTIVITY_TITLE = "Stoploss Inactivity";
    String INTEGRATION_FAILURE_TITLE = "Integration Failure";
    String MATCH_RATE_TITLE = "Match Rate Drop";

    String STOPLOSS_IMPACT = "May lead to inefficient spending on non-performing products";
    String STOPLOSS_INACTIVITY_IMPACT = "Non-performing products may remain active, leading to suboptimal campaign performance";
    String PRODUCTSET_IMPACT = "Campaigns may be disrupted.";
    String INTEGRATION_IMPACT = "Data sync and automation features are disrupted";
    String MATCH_RATE_IMPACT = "Campaign optimization may be hindered due to mismatched catalog data.";

    String STOPLOSS_INACTIVITY_ACTION = "Check your Stoploss settings or contact support if the issue persists";
    String MATCH_RATE_ACTION = "Review your catalog and settings to improve matching";

    String CTA_TEXT_REVIEW_STOPLOSS = "Review Stoploss Settings";

    String STOPLOSS_DESCRIPTION_TEMPLATE = "Stoploss has reached the exclusion limit of %s%% products in your catalog. Additional non-performing products will not be excluded until the limit is adjusted.";
    String STOPLOSS_INACTIVITY_DESCRIPTION_TEMPLATE = "No products have been excluded by Stoploss for the past %d days";
    String INTEGRATION_FAILURE_DESCRIPTION_TEMPLATE = "Connection with %s has failed.";
    String MATCH_RATE_DESCRIPTION_TEMPLATE = "The match percentage for %s has dropped to %s";


    // Actions
    String STOPLOSS_ACTION = "Review or adjust the exclusion limit or reintroduce excluded products to continue optimizing your campaigns.";
    String INTEGRATION_ACTION = "Re-establish the connection to avoid interruptions in product data and automation.";
    String PRODUCTSET_ACTION = "Review or adjust the exclusion limit or reintroduce excluded products to continue optimizing your campaigns.";

    // CTA Texts
    String CTA_TEXT_STOPLOSS = "Reconfigure Exclusion Limit";
    String CTA_TEXT_INTEGRATION = "Reconnect %s";
    String CTA_TEXT_MATCH_RATE = "Improve Match Percentage";

    // URLs
    String CTA_URL_STOPLOSS = "http://example.com/stoploss";
    String CTA_URL_INTEGRATION = "http://example.com/integration";

    String ALERT_CHANNEL_EMAIL = "EMAIL";
    String ALERT_CHANNEL_ALL = "ALL";
    String EMAIL_TEMPLATE = "emailNotificationTemplate";
    String EMAIL_SUBJECT = "Daily Alert Summary: Actions Needed for Stoploss, Productsets & Integrations";
    String DEFAULT_EMAIL = "tech@adyogi.com";
    String DEFAULT_TEAM_NAME = "BigAtom";
    String EMAIL_TOPIC = "email";
    String STOPLOSS_ALERTS = "stoplossAlerts";
    String PRODUCTSETS = "productsets";
    String INTEGRATION_ALERTS = "integrationAlerts";
    String EMAIL = "email";
    String TEAM_NAME = "teamName";
    String FROM_EMAIL = "fromEmail";
    String RECEIPENTS = "recipients";
    String SUBJECT = "subject";
    String BODY = "body";

    String INCIDENT_COMPUTE_TOPIC = "incident-compute";
}



