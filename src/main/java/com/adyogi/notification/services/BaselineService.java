package com.adyogi.notification.services;

import com.adyogi.notification.bigquery.bigquerycomponent.BigQueryClient;
import com.adyogi.notification.components.AlertEntityDTOMapper;
import com.adyogi.notification.database.mongo.entities.Alert;
import com.adyogi.notification.database.sql.entities.Incident;
import com.adyogi.notification.database.sql.entities.Metrics;
import com.adyogi.notification.dto.AlertDTO;
import com.adyogi.notification.dto.TriggerConditionDTO;
import com.adyogi.notification.repositories.back4app.AlertRepository;
import com.adyogi.notification.repositories.mysql.BaselineRepository;

import com.adyogi.notification.database.sql.entities.Baseline;
import com.adyogi.notification.repositories.mysql.IncidentRepository;
import com.adyogi.notification.repositories.mysql.MetricsRepository;
import com.adyogi.notification.utils.logging.LogUtil;
import com.google.cloud.bigquery.InsertAllRequest;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.adyogi.notification.utils.constants.ConfigConstants.*;
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
    AlertRepository alertRepository;

    @Autowired
    MetricsRepository metricsRepository;

    @Autowired
    private AlertEntityDTOMapper alertEntityDTOMapper;

    @PersistenceContext
    private EntityManager entityManager;

    private final Logger logger = LogUtil.getInstance();


    @Cacheable(value = CACHE_ALERT, key = CLIENT_ID_KEY)
    public List<Alert> getAlerts(String clientId) {
        return alertRepository.findAlertsByClientIdAndStatus(clientId,
                String.valueOf(ALERT_STATUS.ENABLED));
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


    public <T extends Alert> void createBaselineUsingNotification(String clientId,
                                                                         List<T> notificationConfigurations,
                                                                         Metrics metrics) {
        List<Baseline> baselines = notificationConfigurations.stream()
                .filter(config -> config.getTriggerConditions().stream()
                        .anyMatch(condition -> condition.getMetricName().equals(metrics.getMetric())))
                .map(config -> baselineRepository.save(Baseline.builder()
                        .clientId(metrics.getClientId())
                        .metric(metrics.getMetric())
                        .objectType(metrics.getObjectType())
                        .objectIdentifier(metrics.getObjectIdentifier())
                        .value(metrics.getValue())
                        .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                        .alertId(config.getObjectId())
                        .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
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
        List<Alert> alerts = getAlerts(clientId);

        if (!alerts.isEmpty()) {
            createBaselineUsingNotification(clientId, alerts, metrics);
        }
        logger.error("No client alerts found for client {}", clientId);
    }

    public Baseline buildBaseline(Metrics metrics, AlertDTO alertDTO) {
        return Baseline.builder()
                .clientId(metrics.getClientId())
                .metric(metrics.getMetric())
                .objectType(metrics.getObjectType())
                .objectIdentifier(metrics.getObjectIdentifier())
                .value(metrics.getValue())
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .alertId(alertDTO.getObjectId())
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();
    }


    public void createBaselineFromAlert(String alertId, String clientId) {

        Alert alert = alertRepository.findByObjectId(alertId , clientId);

        if (alert == null) {
            logger.error("Client Alert not found for alertId: {} and clientId: {}", alertId, clientId);
            return;
        }
        AlertDTO alertDTO = alertEntityDTOMapper.convertEntityToDTO(alert);

        if (alertDTO == null){
            logger.error("Error converting entity to dto", alertId, clientId);
            return;
        }
        List<METRIC> metrics = alertDTO.getTriggerConditions()
                .stream()
                .map(TriggerConditionDTO::getMetric)
                .collect(Collectors.toList());

        if (metrics.isEmpty()) {
            logger.error("metrics does not exists", alertId, clientId);
            return;
        }
        List<Metrics> metricsObj = metricsRepository.findAllByMetricAndClientId(clientId, metrics);
        List <Baseline> baselines = new ArrayList<>();
        for (Metrics metric : metricsObj) {
            baselines.add(buildBaseline(metric, alertDTO));
        }

        if (baselines.isEmpty()) {
            logger.info("No Baselines created for client {}", clientId);
        } else {
            batchSaveBaseline(clientId, baselines);
        }
    }
}
