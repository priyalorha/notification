package com.adyogi.notification.services;

import com.adyogi.notification.bigquery.bigquerycomponent.BigQueryClient;
import com.adyogi.notification.components.DefaultAlertEntityDTOMapper;
import com.adyogi.notification.database.mongo.entities.ClientAlert;
import com.adyogi.notification.database.mongo.entities.DefaultAlert;
import com.adyogi.notification.database.sql.entities.Incident;
import com.adyogi.notification.database.sql.entities.Metrics;
import com.adyogi.notification.dto.DefaultAlertDTO;
import com.adyogi.notification.dto.TriggerConditionDTO;
import com.adyogi.notification.repositories.back4app.ClientAlertRepository;
import com.adyogi.notification.repositories.back4app.DefaultAlertRepository;
import com.adyogi.notification.repositories.mysql.BaselineRepository;

import com.adyogi.notification.database.sql.entities.Baseline;
import com.adyogi.notification.repositories.mysql.IncidentRepository;
import com.adyogi.notification.repositories.mysql.MetricsRepository;
import com.adyogi.notification.utils.constants.TableConstants;
import com.adyogi.notification.utils.logging.LogUtil;
import com.google.cloud.bigquery.InsertAllRequest;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;
import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.adyogi.notification.utils.constants.BigQueryConstants.BATCH_SIZE;
import static com.adyogi.notification.utils.constants.TableConstants.*;

@Service
public class BaselineService {

    @Autowired
    ModelMapper modelMapper;
    @Autowired
    BigQueryClient bigQueryClient;
    @Autowired
    BaselineRepository baselineRepository;
    @Autowired
    IncidentRepository incidentRepository;
    @Autowired
    DefaultAlertRepository defaultAlertRepository;
    @Autowired
    ClientAlertRepository clientAlertRepository;

    @Autowired
    MetricsRepository metricsRepository;

    @Autowired
    private DefaultAlertEntityDTOMapper defaultAlertEntityDTOMapper;

    @PersistenceContext
    private EntityManager entityManager;

    private final Logger logger = LogUtil.getInstance();


    @Cacheable(value = "clientAlerts", key = "#clientId")
    public List<ClientAlert> getClientAlerts(String clientId) {
        return clientAlertRepository.findAlertsByClientIdAndStatus(clientId,
                String.valueOf(STATUS.ENABLED));
    }

    @Cacheable(value = "allClientAlerts", key = "#clientId")
    public List<ClientAlert> getAllActiveClientAlerts(String clientId) {
        return clientAlertRepository.findByClientId(clientId);
    }

    @Cacheable(value = "defaultAlerts")
    public List<DefaultAlert> getDefaultAlert() {
        return defaultAlertRepository.findByStatus(STATUS.ENABLED);
    }

    public List<Incident> fetchIncidents(String clientId) {
        return incidentRepository.findByClientIdAndStatus(clientId, INCIDENT_STATUS.OPEN);
    }

    public void insertBaselineToBigQuery(List<Baseline> baselines) {
        if (baselines.isEmpty()) {
            logger.warn("No baselines to insert into BigQuery.");
            return;
        }

        List<InsertAllRequest.RowToInsert> rows = baselines.stream()
                .map(baseline -> InsertAllRequest.RowToInsert.of(baseline.toMap()))
                .collect(Collectors.toList());

        bigQueryClient.insertRows(rows, BIGQUERY_NOTIFICATION_DATASET_NAME, BIGQUERY_BASELINE_TABLE_NAME);
    }
    @Transactional
    public void batchSaveBaseline(String clientId, List<Baseline> baselines) {
        List<Incident> incidents = fetchIncidents(clientId);
        Map<String, Incident> incidentMap = incidents.stream()
                .collect(Collectors.toMap(Incident::getBaselineId, i -> i));

        List<Baseline> selectedBaselines = baselines.stream()
                .filter(baseline -> !incidentMap.containsKey(baseline.getBaselineId()))
                .collect(Collectors.toList());

        if (!selectedBaselines.isEmpty()) {
            baselineRepository.saveAll(baselines);
        } else {
            logger.warn("No new baselines created for client {}", clientId);
        }

        insertBaselineToBigQuery(baselines);
    }


    public <T extends DefaultAlert> void createBaselineUsingNotification(String clientId,
                                                                         List<T> notificationConfigurations,
                                                                         Metrics metrics) {
        List<Baseline> baselines = notificationConfigurations.stream()
                .filter(config -> config.getTriggerConditions().stream()
                        .anyMatch(condition -> condition.getMetricName().equals(metrics.getMetricName())))
                .map(config -> baselineRepository.save(Baseline.builder()
                        .clientId(metrics.getClientId())
                        .metricName(metrics.getMetricName())
                        .objectType(metrics.getObjectType())
                        .objectId(metrics.getObjectId())
                        .value(metrics.getValue())
                        .valueDataType(metrics.getValueDataType())
                        .createdAt(LocalDateTime.now())
                        .alertId(config.getObjectId())
                        .updatedAt(LocalDateTime.now())
                        .build()))
                .collect(Collectors.toList());

        if (baselines.isEmpty()) {
            logger.info("No Baselines created for client {}", clientId);
        } else {
            batchSaveBaseline(clientId, baselines);
        }
    }

    public void createBaselineFromMetric(Metrics metrics) {
        String clientId = metrics.getClientId();
        List<ClientAlert> clientAlerts = getClientAlerts(clientId);

        if (!clientAlerts.isEmpty()) {
            createBaselineUsingNotification(clientId, clientAlerts, metrics);
        } else {
            createBaselineUsingNotification(clientId, getDefaultAlert(), metrics);
        }
    }

    public Baseline buildBaseline(Metrics metrics, DefaultAlertDTO defaultAlertDTO) {
        return Baseline.builder()
                .clientId(metrics.getClientId())
                .metricName(metrics.getMetricName())
                .objectType(metrics.getObjectType())
                .objectId(metrics.getObjectId())
                .value(metrics.getValue())
                .valueDataType(metrics.getValueDataType())
                .createdAt(LocalDateTime.now())
                .alertId(defaultAlertDTO.getObjectId())
                .updatedAt(LocalDateTime.now())
                .build();
    }


    @Async
    public void generateBaselineForAlerts(String alertId, String clientId) {

        ClientAlert clientAlert = clientAlertRepository.findByObjectIdAndClientId(alertId , clientId);

        if (clientAlert == null) {
            logger.error("Client Alert not found for alertId: {} and clientId: {}", alertId, clientId);
            return;
        }
        DefaultAlertDTO defaultAlertDTO = defaultAlertEntityDTOMapper.convertEntityToDTO(clientAlert);

        if (defaultAlertDTO == null){
            logger.error("Error converting entity to dto", alertId, clientId);
            return;
        }
        List<TableConstants.METRIC_NAME> metricNames = defaultAlertDTO.getTriggerConditions()
                .stream()
                .map(TriggerConditionDTO::getMetricName)
                .collect(Collectors.toList());

        if (metricNames.isEmpty()) {
            logger.error("metrics does not exists", alertId, clientId);
            return;
        }
        List<Metrics> metrics = metricsRepository.findAllByMetricNameAndClientId(clientId, metricNames);
        List <Baseline> baselines = new ArrayList<>();
        for (Metrics metric : metrics) {
            baselines.add(buildBaseline(metric, defaultAlertDTO));
        }

        if (baselines.isEmpty()) {
            logger.info("No Baselines created for client {}", clientId);
        } else {
            batchSaveBaseline(clientId, baselines);
        }
    }

    @Async
    public void generateBaselineForAlerts(String alertId) {
        // Fetch the alert from the repository
        Optional<DefaultAlert> defaultAlertOpt = defaultAlertRepository.findById(alertId);
        if (defaultAlertOpt.isEmpty()) {
            logger.error("Alert not found for alertId: {}", alertId);
            return;
        }

        // Convert entity to DTO
        DefaultAlertDTO defaultAlertDTO = defaultAlertEntityDTOMapper.convertEntityToDTO(defaultAlertOpt.get());
        if (defaultAlertDTO == null) {
            logger.error("Error converting entity to DTO for alertId: {}", alertId);
            return;
        }

        // Extract metric names from alert trigger conditions
        List<TableConstants.METRIC_NAME> metricNames = defaultAlertDTO.getTriggerConditions()
                .stream()
                .map(TriggerConditionDTO::getMetricName)
                .collect(Collectors.toList());

        if (metricNames.isEmpty()) {
            logger.error("No metrics found for alertId: {}", alertId);
            return;
        }

        // Retrieve all distinct client IDs from ClientAlert
        List<String> excludedClientIds = clientAlertRepository.findDistinctClientIds();

        int pageNumber = 0;
        Page<Metrics> metricsPage;

        do {
            // Fetch paginated metrics, excluding specific clients
            Pageable pageable = PageRequest.of(pageNumber, BATCH_SIZE);
            metricsPage = metricsRepository.findAllByMetricNameWhereNotClientId(metricNames, excludedClientIds, pageable);
            List<Metrics> metrics = metricsPage.getContent();

            if (!metrics.isEmpty()) {
                // Transform metrics into baselines and save them in batches
                List<Baseline> baselines = metrics.stream()
                        .map(metric -> buildBaseline(metric, defaultAlertDTO))
                        .collect(Collectors.toList());

                batchSaveBaseline("all", baselines);
            }

            pageNumber++;

        } while (metricsPage.hasNext());

        logger.info("Baseline generation completed for alertId: {}", alertId);
    }
}
