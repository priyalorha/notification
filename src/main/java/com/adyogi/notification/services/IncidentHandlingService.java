package com.adyogi.notification.services;

import com.adyogi.notification.bigquery.bigquerycomponent.BigQueryClient;
import com.adyogi.notification.components.AlertEntityDTOMapper;
import com.adyogi.notification.database.mongo.entities.Alert;
import com.adyogi.notification.database.sql.entities.Baseline;
import com.adyogi.notification.database.sql.entities.Incident;
import com.adyogi.notification.database.sql.entities.Metrics;
import com.adyogi.notification.dto.AlertDTO;
import com.adyogi.notification.repositories.back4app.AlertRepository;
import com.adyogi.notification.repositories.mysql.BaselineRepository;
import com.adyogi.notification.repositories.mysql.IncidentRepository;
import com.adyogi.notification.repositories.mysql.MetricsRepository;
import com.adyogi.notification.utils.constants.TableConstants;
import com.adyogi.notification.utils.logging.LogUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.bigquery.InsertAllRequest;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static com.adyogi.notification.utils.constants.ConfigConstants.*;
import static com.adyogi.notification.utils.constants.TableConstants.*;

@Component
public class IncidentHandlingService {

    private final Logger logger = LogUtil.getInstance();

    @Autowired
    private AlertEntityDTOMapper alertEntityDTOMapper;

    @Autowired
    private ModelMapper modelMapper;


    @Autowired
    BaselineRepository baselineRepository;

    @Autowired
    AlertRepository alertRepository;

    @Autowired
    BigQueryClient bigQueryConfiguration;

    @Autowired
    IncidentRepository incidentRepository;

    @Autowired
    BaselineService baselineService;

    @Autowired
    MetricsRepository metricsRepository;

    @Autowired
    ObjectMapper objectMapper;


    // Fetch active client notification configurations
    public List<Alert> fetchActiveClientNotification(String clientId) {
        try {
            logger.info("Fetching active notifications for clientId: {}", clientId);
            return alertRepository.findAlertsByClientIdAndStatus(clientId, String.valueOf(ALERT_STATUS.ENABLED));
        } catch (Exception e) {
            logger.error("Error fetching client notification configurations for clientId: {}", clientId, e);
            throw e;
        }
    }


    // Fetch baselines for the given metric
    public List<Baseline> fetchBaselinesFromMetric(Metrics metrics) {
        try {
            logger.info("Fetching baselines for metric: {}", metrics.getMetric());
            return baselineRepository.findBaselineByIdExceptAlertId(
                    metrics.getClientId(),
                    metrics.getMetric(),
                    metrics.getObjectType(),
                    metrics.getObjectIdentifier()
            );
        } catch (Exception e) {
            logger.error("Error fetching baselines for metric: {}", metrics.getMetric(), e);
            throw e;
        }
    }

    // Get an incident based on baseline, notification config, and metric
    private Incident getIncident(Baseline baseline,
                                           Metrics metrics) {
        Optional <Incident> incident;
        try {
            logger.info("Fetching incident for baseline: {} and metric: {}",
                    baseline.getAlertId(), metrics.getMetric());
            incident =  incidentRepository.findByAlertIdAndClientIdAndMetricAndObjectTypeAndObjectIdentifierAndIncidentStatus(
                    baseline.getAlertId(),
                    baseline.getClientId(),
                    baseline.getMetric(),
                    baseline.getObjectType(),
                    baseline.getObjectIdentifier(),
                    INCIDENT_STATUS.OPEN);

            if (incident.isEmpty()) {
                return null;
            }
            else {
                return incident.get();
            }
        } catch (Exception e) {
            logger.error("Error fetching incident for baseline: {}", baseline.getAlertId(), e);
            return null;
        }
    }

    // Insert incident to BigQuery
    public void insertIncidentToBigQuery(Incident incident) {
        try {
            logger.info("Inserting incident to BigQuery: {}", incident.getAlertId());
            InsertAllRequest.RowToInsert rowToInsert = InsertAllRequest.RowToInsert.of(incident.toMap());
            bigQueryConfiguration.insertRows(Collections.singletonList(rowToInsert),
                    BIGQUERY_NOTIFICATION_DATASET_NAME,
                    BIGQUERY_INCIDENTS_TABLE_NAME);
        } catch (Exception e) {
            logger.error("Error inserting incident to BigQuery: {}", incident.getAlertId(), e);
            throw e;
        }
    }

    // Create an incident from the baseline and notification configuration
    private Incident createIncident(Baseline baseline, Alert notificationConfig, Metrics metrics) {
        try {
            logger.info("Creating incident for baseline: {} and notificationConfig: {}", baseline.getAlertId(),
                    notificationConfig.getObjectId());
            return Incident.builder().incidentId(System.currentTimeMillis())
                    .alertId(baseline.getAlertId())
                    .clientId(baseline.getClientId())
                    .metric(baseline.getMetric())
                    .objectType(baseline.getObjectType())
                    .objectIdentifier(baseline.getObjectIdentifier())
                    .alertChannel(notificationConfig.getAlertChannel())
                    .message(notificationConfig.getMessage())
                    .notificationStatus(TableConstants.NOTIFICATION_STATUS.PENDING)
                    .incidentStatus(TableConstants.INCIDENT_STATUS.OPEN)
                    .status(ALERT_STATUS.ENABLED)
                    .baseValue(baseline.getValue())
                    .value(metrics.getValue())
                    .valueDataType(metrics.getValueDataType())
                    .alertResendIntervalMin(notificationConfig.getAlertResendIntervalMin())
                    .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                    .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                    .build();
        } catch (Exception e) {
            logger.error("Error creating incident for baseline: {}", baseline.getAlertId(), e);
            throw e;
        }
    }

    // Save incident to repository
    @Transactional
    public void saveIncident(Incident incident) {
        try {
            logger.info("Saving incident: {}", incident.getAlertId());
            incidentRepository.save(incident);
            incidentRepository.flush();
            logger.info("Incident saved successfully: {}", incidentRepository.findAll()); // Add this
        } catch (Exception e) {
            logger.error("Error saving incident: {}", incident.getAlertId(), e);
            throw e;
        }
    }

    // Generate notification config map (client-specific + default configurations)
    @Cacheable(value = CACHE_NOTIFICATION_CONFIG_MAP, key = CLIENT_ID_KEY)
    private Map<String, Alert> generateNotificationConfigMap(String clientId) {
        Map<String, Alert> notificationConfigMap = new HashMap<>();

        List<Alert> alert = fetchActiveClientNotification(clientId);

        // Map client-specific notifications
        for (Alert notification : alert) {
            notificationConfigMap.put(notification.getObjectId(), notification);
        }


        return notificationConfigMap;
    }

    private void handleIncidentCreationOrUpdate(Baseline baseline, Metrics metrics, Alert notificationConfig) {
        Incident existingIncident = getIncident(baseline, metrics);

        if (existingIncident == null) {
            Incident newIncident = createIncident(baseline, notificationConfig, metrics);
            saveIncident(newIncident);
        } else {
            existingIncident.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
            existingIncident.setValue(metrics.getValue());
            saveIncident(existingIncident);
        }
    }

    // Handle resolution of incidents
    private void handleIncidentResolution(Baseline baseline, Metrics metrics) {
        Incident existingIncident = getIncident(baseline, metrics);

        if (existingIncident != null) {
            existingIncident.setValue(metrics.getValue());
            existingIncident.setIncidentStatus(TableConstants.INCIDENT_STATUS.RESOLVED);
            existingIncident.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));

            saveIncident(existingIncident);
            insertIncidentToBigQuery(existingIncident);
        }
    }

    private void evaluateRuleWrtBaselineAndMetric(Metrics metrics, Baseline baseline, Map<String, Alert> notificationConfigMap)  {
        Alert notificationConfig = notificationConfigMap.get(baseline.getAlertId());

        if (notificationConfig != null) {
            AlertDTO defaultAlertDTO = alertEntityDTOMapper.convertEntityToDTO(notificationConfig);
            // TODO should be changed to accomodate multiple alert conditions
            boolean canIncidentBeCreated = new EvaluateTriggerCondition().evaluateCondition(
                    defaultAlertDTO.getTriggerConditions().get(0),
                    metrics, baseline);

            if (canIncidentBeCreated) {
                handleIncidentCreationOrUpdate(baseline, metrics, notificationConfig);
            } else {
                handleIncidentResolution(baseline, metrics);
            }
        } else {
            logger.error("No notification configuration found for baseline: {}", baseline.getAlertId());
        }
    }

    public void handleMissingBaselineWrtAlerts(Metrics metrics, List<Baseline> baselines, Map<String, Alert> notificationConfigMap)
    {
        if (baselines.isEmpty()) {
            baselineService.createBaselineFromMetric( metrics);
            logger.error("No baselines found for metric: {}", metrics.getMetric());
            return;
        }

        Map<String, Baseline> baselineMap = new HashMap<>();
        for (Baseline base : baselines) {
            baselineMap.put(base.getAlertId(), base);
        }

        for (String alertId : notificationConfigMap.keySet()) {
            if (notificationConfigMap.get(alertId).getTriggerConditions().get(0).getMetricName().equals(metrics.getMetric()) &&
            !baselineMap.containsKey(alertId)) {
                baselineService.createBaselineFromAlert(alertId, metrics.getClientId());
            }
        }

    }


// Process a new metric and create incidents if necessary
    public void evaluateAndCreateIncidentFromMetric(Metrics metrics,  Map<String, Alert> notificationConfigMap ) {
        if(notificationConfigMap.isEmpty())
        {
            logger.error("No notification configuration found for client: {}", metrics.getClientId());
        }
        try {

            handleMissingBaselineWrtAlerts(metrics, fetchBaselinesFromMetric(metrics), notificationConfigMap);
            //calling in case new baseline is created,
            List<Baseline> baselines = fetchBaselinesFromMetric(metrics);
            logger.info("Length baseline: " + baselines.size());
            // Process each baseline
            for (Baseline baseline : baselines) {
                evaluateRuleWrtBaselineAndMetric(metrics, baseline, notificationConfigMap);
            }
        } catch (Exception e) {
            logger.error("Error processing new metric: {}", metrics.getMetric(), e);
            throw e;
        }
    }


    @Async(PROCESS_BULK_INCIDENT_TASK)
    public void processIncidentsForClient(String clientId)
    {
        Map<String, Alert> notificationConfigMap = generateNotificationConfigMap(clientId);

        if(notificationConfigMap.isEmpty())
        {
            logger.error("No notification configuration found for client: {}", clientId);
            return;
        }
        List<Metrics> metrics = metricsRepository.findMetricByClientId(clientId);
        for (Metrics metric : metrics) {
            evaluateAndCreateIncidentFromMetric(metric, notificationConfigMap);
        }
    }
}

