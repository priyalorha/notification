package com.adyogi.notification.services;

import com.adyogi.notification.database.sql.entities.Incident;
import com.adyogi.notification.dto.emails.EmailAlertSummaryDTO;
import com.adyogi.notification.dto.emails.IntegrationAlertDTO;
import com.adyogi.notification.dto.emails.ProductsetAlertDTO;
import com.adyogi.notification.dto.emails.StoplossAlertDTO;
import com.adyogi.notification.utils.constants.AlertConstants;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class AlertSummaryService {

    public EmailAlertSummaryDTO prepareAlertSummary(List<Incident> incidents) {
        EmailAlertSummaryDTO summary = new EmailAlertSummaryDTO();
        List<StoplossAlertDTO> stoplossAlerts = new ArrayList<>();
        List<ProductsetAlertDTO> productsets = new ArrayList<>();
        List<IntegrationAlertDTO> integrationAlerts = new ArrayList<>();

        for (Incident incident : incidents) {
            switch (incident.getMetricName().toString()) {
                case AlertConstants.STOP_LOSS_EXCLUSION_COUNT:
                    stoplossAlerts.add(createStoplossLimitReachedAlert(incident));
                    break;

                case AlertConstants.STOPLOSS_INACTIVITY:
                    stoplossAlerts.add(createStoplossInactivityAlert(incident));
                    break;

                case AlertConstants.PRODUCT_SET_COUNT:
                    productsets.add(createProductsetAlert(incident));
                    break;

                case AlertConstants.INTEGRATION_FAILURE:
                    integrationAlerts.add(createIntegrationFailureAlert(incident));
                    break;

                case AlertConstants.MATCH_RATE:
                    integrationAlerts.add(createMatchRateAlert(incident));
                    break;

                default:
                    break;
            }
        }

        summary.setStoplossAlerts(stoplossAlerts);
        summary.setProductSets(productsets);
        summary.setIntegrationAlerts(integrationAlerts);

        return summary;
    }
    private StoplossAlertDTO createStoplossLimitReachedAlert(Incident incident) {
        StoplossAlertDTO alert = new StoplossAlertDTO();
        alert.setDescription(String.format(incident.getMessage(), incident.getValue()));
        alert.setTitle(AlertConstants.STOPLOSS_LIMIT_REACHED_TITLE);
        alert.setImpact(AlertConstants.STOPLOSS_IMPACT);
        alert.setAction(AlertConstants.STOPLOSS_ACTION);
        alert.setCtaText(AlertConstants.CTA_TEXT_STOPLOSS);
        alert.setCtaUrl(AlertConstants.CTA_URL_STOPLOSS);
        return alert;
    }

    private StoplossAlertDTO createStoplossInactivityAlert(Incident incident) {
        StoplossAlertDTO alert = new StoplossAlertDTO();
        long daysBetween = ChronoUnit.DAYS.between(incident.getCreatedAt(), LocalDateTime.now());
        alert.setDescription(String.format(AlertConstants.STOPLOSS_INACTIVITY_DESCRIPTION_TEMPLATE, daysBetween));
        alert.setImpact(AlertConstants.STOPLOSS_INACTIVITY_IMPACT);
        alert.setTitle(AlertConstants.STOPLOSS_INACTIVITY_TITLE);
        alert.setAction(AlertConstants.STOPLOSS_INACTIVITY_ACTION);
        alert.setCtaText(AlertConstants.CTA_TEXT_REVIEW_STOPLOSS);
        alert.setCtaUrl(AlertConstants.CTA_URL_STOPLOSS);
        return alert;
    }

    private ProductsetAlertDTO createProductsetAlert(Incident incident) {
        ProductsetAlertDTO alert = new ProductsetAlertDTO();
        alert.setAction(AlertConstants.PRODUCTSET_ACTION);
        alert.setName(incident.getObjectId());
        alert.setImpact(AlertConstants.PRODUCTSET_IMPACT);
        return alert;
    }

    private IntegrationAlertDTO createIntegrationFailureAlert(Incident incident) {
        IntegrationAlertDTO alert = new IntegrationAlertDTO();
        alert.setAction(AlertConstants.INTEGRATION_ACTION);
        alert.setTitle(AlertConstants.INTEGRATION_FAILURE_TITLE);
        alert.setDescription(String.format(AlertConstants.INTEGRATION_FAILURE_DESCRIPTION_TEMPLATE, incident.getObjectType().toString()));
        alert.setCtaText(String.format(AlertConstants.CTA_TEXT_INTEGRATION, incident.getObjectType().toString()));
        alert.setImpact(AlertConstants.INTEGRATION_IMPACT);
        alert.setCtaUrl(AlertConstants.CTA_URL_INTEGRATION);
        return alert;
    }

    private IntegrationAlertDTO createMatchRateAlert(Incident incident) {
        IntegrationAlertDTO alert = new IntegrationAlertDTO();
        alert.setTitle(AlertConstants.MATCH_RATE_TITLE);
        alert.setAction(AlertConstants.MATCH_RATE_ACTION);
        alert.setDescription(String.format(AlertConstants.MATCH_RATE_DESCRIPTION_TEMPLATE, incident.getObjectType().toString(), incident.getValue()));
        alert.setCtaText(AlertConstants.CTA_TEXT_MATCH_RATE);
        alert.setImpact(AlertConstants.MATCH_RATE_IMPACT);
        return alert;
    }

}
