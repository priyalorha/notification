package com.adyogi.notification.services;

import com.adyogi.notification.bigquery.bigquerycomponent.BigQueryClient;
import com.adyogi.notification.components.DefaultAlertEntityDTOMapper;
import com.adyogi.notification.database.mongo.entities.ClientAlert;
import com.adyogi.notification.database.mongo.entities.DefaultAlert;
import com.adyogi.notification.database.sql.entities.Baseline;
import com.adyogi.notification.database.sql.entities.Incident;
import com.adyogi.notification.database.sql.entities.Metrics;
import com.adyogi.notification.dto.DefaultAlertDTO;
import com.adyogi.notification.repositories.back4app.ClientAlertRepository;
import com.adyogi.notification.repositories.back4app.DefaultAlertRepository;
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
import java.util.*;

import static com.adyogi.notification.utils.constants.TableConstants.*;

@Component
public class IncidentHandlingService {

    private final Logger logger = LogUtil.getInstance();

    @Autowired
    private DefaultAlertEntityDTOMapper defaultAlertEntityDTOMapper;

    @Autowired
    private ModelMapper modelMapper;


    @Autowired
    BaselineRepository baselineRepository;

    @Autowired
    ClientAlertRepository clientAlertRepository;

    @Autowired
    DefaultAlertRepository defaultAlertRepository;
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
    public List<ClientAlert> fetchActiveClientNotification(String clientId) {
        try {
            logger.info("Fetching active notifications for clientId: {}", clientId);
            return clientAlertRepository.findAlertsByClientIdAndStatus(clientId, String.valueOf(STATUS.ENABLED));
        } catch (Exception e) {
            logger.error("Error fetching client notification configurations for clientId: {}", clientId, e);
            throw e;
        }
    }

    public List<ClientAlert> fetchDeactiveClientNotification(String clientId) {
        try {
            logger.info("Fetching active notifications for clientId: {}", clientId);
            return clientAlertRepository.findAlertsByClientIdAndStatus(clientId, String.valueOf(STATUS.DISABLED));
        } catch (Exception e) {
            logger.error("Error fetching client notification configurations for clientId: {}", clientId, e);
            throw e;
        }
    }

    // Fetch default notification configurations
    public List<DefaultAlert> fetchDefaultNotificationConfiguration() {
        try {
            logger.info("Fetching default notification configurations");
            return defaultAlertRepository.findByStatus(STATUS.ENABLED);
        } catch (Exception e) {
            logger.error("Error fetching default notification configurations", e);
            throw e;
        }
    }

    // Fetch baselines for the given metric
    public List<Baseline> fetchBaselinesFromMetric(Metrics metrics) {
        try {
            logger.info("Fetching baselines for metric: {}", metrics.getMetricName());
            return baselineRepository.findBaselineByIdExceptAlertId(
                    metrics.getClientId(),
                    metrics.getMetricName(),
                    metrics.getObjectType(),
                    metrics.getObjectId()
            );
        } catch (Exception e) {
            logger.error("Error fetching baselines for metric: {}", metrics.getMetricName(), e);
            throw e;
        }
    }

    // Get an incident based on baseline, notification config, and metric
    private Incident getIncident(Baseline baseline,
                                           Metrics metrics) {
        Optional <Incident> incident;
        try {
            logger.info("Fetching incident for baseline: {} and metric: {}",
                    baseline.getAlertId(), metrics.getMetricName());
            incident =  incidentRepository.findByAlertIdAndClientIdAndMetricNameAndObjectTypeAndObjectIdAndIncidentStatus(
                    baseline.getAlertId(),
                    baseline.getClientId(),
                    baseline.getMetricName(),
                    baseline.getObjectType(),
                    baseline.getObjectId(),
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
    private Incident createIncident(Baseline baseline, ClientAlert notificationConfig, Metrics metrics) {
        try {
            logger.info("Creating incident for baseline: {} and notificationConfig: {}", baseline.getAlertId(), notificationConfig.getObjectId());
            return Incident.builder().incidentId(System.currentTimeMillis())
                    .alertId(baseline.getAlertId())
                    .clientId(baseline.getClientId())
                    .metricName(baseline.getMetricName())
                    .objectType(baseline.getObjectType())
                    .objectId(baseline.getObjectId())
                    .alertChannel(notificationConfig.getAlertChannel())
                    .message(notificationConfig.getMessage())
                    .notificationStatus(TableConstants.NOTIFICATION_STATUS.PENDING)
                    .incidentStatus(TableConstants.INCIDENT_STATUS.OPEN)
                    .status(TableConstants.STATUS.ENABLED)
                    .baseValue(baseline.getValue())
                    .value(metrics.getValue())
                    .valueDataType(metrics.getValueDataType())
                    .alertResendIntervalMin(notificationConfig.getAlertResendIntervalMin())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
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
    @Cacheable(value = "notificationConfigMap", key = "#clientId")
    private Map<String, ClientAlert> generateNotificationConfigMap(String clientId) {
        Map<String, ClientAlert> notificationConfigMap = new HashMap<>();

        List<ClientAlert> clientAlert = fetchActiveClientNotification(clientId);

        //should not check for default client alert if even a single alert has been configured
        if (clientAlert.isEmpty() && !fetchDeactiveClientNotification(clientId).isEmpty()) {
            logger.error("No client notification configuration found for clientId: {}", clientId);
            return notificationConfigMap;
        }

        // Map client-specific notifications
        for (ClientAlert notification : clientAlert) {
            notificationConfigMap.put(notification.getObjectId(), notification);
        }

        // Map default notifications
        for (DefaultAlert defaultNotification : fetchDefaultNotificationConfiguration()) {
            ClientAlert d = modelMapper.map(defaultNotification, ClientAlert.class);
            notificationConfigMap.put(defaultNotification.getObjectId(), d);
        }

        return notificationConfigMap;
    }

    private void handleIncidentCreationOrUpdate(Baseline baseline, Metrics metrics, ClientAlert notificationConfig) {
        Incident existingIncident = getIncident(baseline, metrics);

        if (existingIncident == null) {
            Incident newIncident = createIncident(baseline, notificationConfig, metrics);
            saveIncident(newIncident);
        } else {
            existingIncident.setUpdatedAt(LocalDateTime.now());
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
            existingIncident.setUpdatedAt(LocalDateTime.now());

            saveIncident(existingIncident);
            insertIncidentToBigQuery(existingIncident);
        }
    }

    private void evaluateBaselineAgainstMetrics(Metrics metrics, Baseline baseline, Map<String, ClientAlert> notificationConfigMap)  {
        ClientAlert notificationConfig = notificationConfigMap.get(baseline.getAlertId());

        if (notificationConfig != null) {
            DefaultAlertDTO defaultAlertDTO = defaultAlertEntityDTOMapper.convertEntityToDTO(notificationConfig);
            // TODO should be changed to accomodate multiple alert conditions
            boolean canIncidentBeCreated = new EvaluateTriggerCondition(
                    defaultAlertDTO.getTriggerConditions().get(0),
                    baseline, metrics).evaluateCondition();

            if (canIncidentBeCreated) {
                handleIncidentCreationOrUpdate(baseline, metrics, notificationConfig);
            } else {
                handleIncidentResolution(baseline, metrics);
            }
        } else {
            logger.error("No notification configuration found for baseline: {}", baseline.getAlertId());
        }
    }

    @Async
// Process a new metric and create incidents if necessary
    public void evaluateAndCreateIncidentFromMetric(Metrics metrics,  Map<String, ClientAlert> notificationConfigMap ) {
        if(notificationConfigMap.isEmpty())
        {
            logger.error("No notification configuration found for client: {}", metrics.getClientId());
        }
        try {
            List<Baseline> baselines = fetchBaselinesFromMetric(metrics);
            if (baselines.isEmpty()) {
                baselineService.createBaselineFromMetric( metrics);
                logger.error("No baselines found for metric: {}", metrics.getMetricName());
                return;
            }

            logger.info(notificationConfigMap);
            logger.info("Length baseline: " + baselines.size());

            // Process each baseline
            for (Baseline baseline : baselines) {
                evaluateBaselineAgainstMetrics(metrics, baseline, notificationConfigMap);
            }
        } catch (Exception e) {
            logger.error("Error processing new metric: {}", metrics.getMetricName(), e);
            throw e;
        }
    }

    @Async
    public void processIncidentsForClient(String clientId)
    {
        Map<String, ClientAlert> notificationConfigMap = generateNotificationConfigMap(clientId);

        if(notificationConfigMap.isEmpty())
        {
            logger.error("No notification configuration found for client: {}", clientId);
            throw new RuntimeException("No notification configuration found for client: " + clientId);
        }
        List<Metrics> metrics = metricsRepository.findMetricByClientId(clientId);
        for (Metrics metric : metrics) {
            evaluateAndCreateIncidentFromMetric(metric, notificationConfigMap);
        }
    }
}

