package com.adyogi.notification.services;

import com.adyogi.notification.bigquery.bigquerycomponent.BigQueryClient;
import com.adyogi.notification.database.sql.entities.Metrics;
import com.adyogi.notification.dto.BigQueryMetricsDTO;
import com.adyogi.notification.dto.MetricsDTO;
import com.adyogi.notification.repositories.mysql.MetricsRepository;
import com.adyogi.notification.utils.logging.LogUtil;
import com.google.cloud.bigquery.InsertAllRequest;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.adyogi.notification.utils.constants.TableConstants.*;

@Service
@RequiredArgsConstructor
public class MetricsService {


    private static final Logger logger = LogUtil.getInstance();

    @Autowired
    private final MetricsRepository metricsRepository;
    @Autowired
    private final BigQueryClient bigQueryConfiguration;
    @Autowired
    private final EntityManager entityManager;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Metrics saveMetrics(String clientId, MetricsDTO metricDTO) {

        Metrics metrics = modelMapper.map(metricDTO, Metrics.class);

        metrics.setClientId(clientId);
        metrics.setCreatedAt(LocalDateTime.now());
        metrics.setUpdatedAt(LocalDateTime.now());

        return metricsRepository.saveAndFlush(metrics);
    }

    @Transactional
    public void bulkUpsertMetrics(List<Metrics> metricsList) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(INSERT_METRICS_SQL_RAW_QUERY);
        for (int i = 0; i < metricsList.size(); i++) {
            Metrics metric = metricsList.get(i);
            metric.setCreatedAt(LocalDateTime.now());
            metric.setUpdatedAt(LocalDateTime.now());
            queryBuilder.append(String.format(
                    "('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')",
                    metric.getClientId(),
                    metric.getMetricName(),
                    metric.getValue(),
                    metric.getValueDataType(),
                    metric.getCreatedAt(),
                    metric.getUpdatedAt(),
                    metric.getObjectType(),
                    metric.getObjectId()
            ));
            if (i < metricsList.size() - 1) {
                queryBuilder.append(", ");
            }
        }

        queryBuilder.append(INSERT_METRICS_SQL_RAW_QUERY_DUPLICATE_KEY_UPDATE_SQL);
        try {
            entityManager.createNativeQuery(queryBuilder.toString()).executeUpdate();
        } catch (javax.persistence.TransactionRequiredException e) {
            logger.error("Error while bulk upserting metrics", e);
        }

    }

    public void insertMetricToBigQuery(BigQueryMetricsDTO metrics) {
        InsertAllRequest.RowToInsert rowToInsert = InsertAllRequest.RowToInsert.of(metrics.toMap());
        bigQueryConfiguration.insertRows(Collections.singletonList(rowToInsert),
                BIGQUERY_NOTIFICATION_DATASET_NAME,
                BIGQUERY_METRICS_TABLE_NAME);
    }

    public void insertMetricsToBigQuery(List<BigQueryMetricsDTO> metrics) {
        List<InsertAllRequest.RowToInsert> rowsToInsert = metrics.stream()
                .map(metric -> InsertAllRequest.RowToInsert.of(metric.toMap()))
                .collect(Collectors.toList());

        bigQueryConfiguration.insertRows(rowsToInsert,
                BIGQUERY_NOTIFICATION_DATASET_NAME,
                BIGQUERY_METRICS_TABLE_NAME);
    }
}
