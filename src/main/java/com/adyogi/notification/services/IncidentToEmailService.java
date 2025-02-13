package com.adyogi.notification.services;

import com.adyogi.notification.database.mongo.entities.AlertChannel;
import com.adyogi.notification.database.sql.entities.Incident;
import com.adyogi.notification.repositories.back4app.AlertChannelRepository;
import com.adyogi.notification.repositories.back4app.AlertRepository;
import com.adyogi.notification.repositories.mysql.IncidentRepository;
import com.adyogi.notification.utils.logging.LogUtil;
import net.sf.ehcache.CacheManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import com.adyogi.notification.dto.emails.EmailAlertSummaryDTO;
import com.adyogi.notification.utils.constants.TableConstants;
import org.thymeleaf.context.Context;

import java.time.ZoneOffset;
import java.util.*;


import java.time.Duration;
import java.time.LocalDateTime;

import static com.adyogi.notification.utils.constants.AlertConstants.*;
import static com.adyogi.notification.utils.constants.ConfigConstants.*;

@Service
public class IncidentToEmailService{


    @Autowired
    IncidentRepository incidentRepository;
    @Autowired
    AlertChannelRepository alertChannelRepository;
    @Autowired
    AlertRepository alertRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    PubSubPublisher pubSubPublisher;


    @Autowired
    CacheManager cacheManager;

    @Autowired
    TemplateEngine templateEngine;

    private final Logger logger = LogUtil.getInstance();

    public Map<String, AlertChannel> fetchClientCommunicationChannel(String clientId) {
        List<AlertChannel> alertChannels =
                alertChannelRepository.findByClientId(clientId);

        Map<String, AlertChannel> clientCommunicationChannelMap = new HashMap<>();

        for (AlertChannel alertChannel : alertChannels) {
            clientCommunicationChannelMap.put(alertChannel.getAlertChannel().toString(), alertChannel);
        }

        return clientCommunicationChannelMap;
    }

    public String constructEmailBody(List<Incident> incidents) {
        Context context = new Context();

        AlertSummaryService alertSummaryService = new AlertSummaryService();
        EmailAlertSummaryDTO emailAlertSummaryDTO = alertSummaryService.prepareAlertSummary(incidents);

        context.setVariable(STOPLOSS_ALERTS, emailAlertSummaryDTO.getStoplossAlerts());
        context.setVariable(PRODUCTSETS, emailAlertSummaryDTO.getProductSets());
        context.setVariable(INTEGRATION_ALERTS, emailAlertSummaryDTO.getIntegrationAlerts());
        context.setVariable(EMAIL, DEFAULT_EMAIL);
        context.setVariable(TEAM_NAME, DEFAULT_TEAM_NAME);

        return templateEngine.process(EMAIL_TEMPLATE, context);
    }

    public void createEmailAndEnqueue(AlertChannel emailConfiguration,
                                      List<Incident> incidents)  {

        Map<String, Object> emailBody = new HashMap<>();

        emailBody.put(FROM_EMAIL, emailConfiguration.getCommunicationConfiguration().getFromEmail());
        emailBody.put(RECEIPENTS, emailConfiguration.getCommunicationConfiguration().getToEmail());
        emailBody.put(SUBJECT, EMAIL_SUBJECT);
        emailBody.put(BODY, constructEmailBody(incidents));

        pubSubPublisher.publishToPubSub(EMAIL_TOPIC, emailBody);
    }

    private boolean isEligibleForEmail(Incident incident) {
        List <TableConstants.ALERT_CHANNEL> alertChannel = incident.getAlertChannel(); // Normalize to lowercase once
        boolean isEmailChannel = alertChannel.contains(ALERT_CHANNEL_EMAIL) || alertChannel.contains(ALERT_CHANNEL_ALL);

        if (incident.getNotificationSentAt() == null || incident.getAlertResendIntervalMin() == null) {
            return isEmailChannel; // Eligible if never notified and correct channel
        }

        LocalDateTime notificationTime = incident.getNotificationSentAt();
        long minutesSinceNotification = Duration.between(notificationTime, LocalDateTime.now(ZoneOffset.UTC)).toMinutes();
        return minutesSinceNotification >= incident.getAlertResendIntervalMin() && isEmailChannel; // Check time and channel
    }

    @Async(SEND_EMAIL_SINGLE_CLIENT)
    public void sendEmail(String clientId) {
        List<Incident> incidents = incidentRepository.findOpenEnabledIncidents(clientId,
                TableConstants.INCIDENT_STATUS.OPEN,
                TableConstants.ALERT_STATUS.ENABLED);

        if (incidents.isEmpty()) {
            logger.info("No open enabled incidents found for clientId: {}", clientId);
            return;
        }
        // Fetch communication channel only once
        Map<String, AlertChannel> clientCommunicationChannelMap = fetchClientCommunicationChannel(clientId);

        List<Incident> incidentToBeEmailed = new ArrayList<>();

        for (Incident incident : incidents) {
            if (isEligibleForEmail(incident)) {
                incidentToBeEmailed.add(incident);
                incident.setNotificationSentAt(LocalDateTime.now(ZoneOffset.UTC));
            } else {
                logger.info("Incident with alertId: {} has already been sent a " +
                                "notification within the last {} minutes",
                        incident.getAlertId(),
                        incident.getAlertResendIntervalMin());
            }
        }

        // Process email notifications only once
        if (!incidentToBeEmailed.isEmpty()) {
            AlertChannel emailChannel = clientCommunicationChannelMap.get(ALERT_CHANNEL_EMAIL);

            if (emailChannel != null) {
                createEmailAndEnqueue(emailChannel, incidentToBeEmailed);
            } else {
                logger.info("No email configuration found for clientId: {}", clientId);
            }

            // Batch save instead of multiple writes inside the loop
            incidentRepository.saveAll(incidentToBeEmailed);
        }
    }

    @Async(ALL_CLIENT_EMAIL)
    public void sendEmailsForAllClient() {
        List<String> clientIds = incidentRepository.findClientIdsWithOpenIncidents(TableConstants.INCIDENT_STATUS.OPEN,
                TableConstants.ALERT_STATUS.ENABLED);
        for (String clientId : clientIds) {
            pubSubPublisher.publishToPubSub(GENERATE_EMAIL_SUB_TOPIC, clientId);
        }
    }
}
