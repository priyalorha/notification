package com.adyogi.notification.services;

import com.adyogi.notification.database.sql.entities.Metrics;
import com.adyogi.notification.dto.BigQueryMetricsDTO;
import com.adyogi.notification.dto.MetricsDTO;
import com.adyogi.notification.exceptions.NotFoundException;
import com.adyogi.notification.exceptions.ServiceException;
import com.adyogi.notification.utils.logging.LogUtil;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.adyogi.notification.utils.constants.ErrorConstants.*;

@Service
public class MetricsProcessingService {

    private static final Logger logger = LogUtil.getInstance();

    private final ClientValidationService clientValidationService;
    private final MetricsService metricsService;
    private final ModelMapper modelMapper;

    public MetricsProcessingService(ClientValidationService clientValidationService,
                                    MetricsService metricsService,
                                    ModelMapper modelMapper) {
        this.clientValidationService = clientValidationService;
        this.metricsService = metricsService;
        this.modelMapper = modelMapper;
    }

    public BigQueryMetricsDTO saveMetrics(String clientId, MetricsDTO metricRequest) {
        validateClientId( clientId);
        try {
            Metrics savedMetric = metricsService.saveMetrics(clientId, metricRequest);
            BigQueryMetricsDTO bigQueryMetricsDTO = convertToBigQueryDTO(savedMetric);
            metricsService.insertMetricToBigQuery(bigQueryMetricsDTO);
            return bigQueryMetricsDTO;
        }
        catch (Exception e) {
            logger.error(String.format("SOME ERROR  ", metricRequest), e);
            throw new ServiceException(String.format(ERROR_SAVING_METRIC, metricRequest));
        }
    }

    @Cacheable("validateClientId")
    private void validateClientId(String clientId) {

        if (!clientValidationService.isClientIdValid(clientId)) {
            logger.error(String.format(INVALID_CLIENT_ID, clientId));
            throw new NotFoundException(String.format(INVALID_CLIENT_ID, clientId));
        }
    }

    @Async
    public void BulkSaveWithoutClientId(List<MetricsDTO> metricsRequest) {
        logger.info("Starting bulk processing of metrics...");

        List<Metrics> metrics = metricsRequest.stream()
                .map(this::convertToMetricsEntity)
                .collect(Collectors.toList());

        List<BigQueryMetricsDTO> bigQueryMetrics = metricsRequest.stream()
                .map(dto -> {
                    BigQueryMetricsDTO bigQueryMetric = modelMapper.map(dto, BigQueryMetricsDTO.class);
                    bigQueryMetric.setCreatedAt(LocalDateTime.now().toString());
                    bigQueryMetric.setUpdatedAt(LocalDateTime.now().toString());
                    return bigQueryMetric;
                })
                .collect(Collectors.toList());

        metricsService.bulkUpsertMetrics(metrics);
        metricsService.insertMetricsToBigQuery(bigQueryMetrics);
        logger.info("Completed metrics bulk processing...");
    }

    private BigQueryMetricsDTO convertToBigQueryDTO(Metrics metrics) {
        return BigQueryMetricsDTO.builder()
                .clientId(metrics.getClientId())
                .metricName(String.valueOf(metrics.getMetricName()))
                .objectId(metrics.getObjectId())
                .objectType(String.valueOf(metrics.getObjectType()))
                .value(metrics.getValue())
                .valueDataType(metrics.getValueDataType())
                .createdAt(metrics.getCreatedAt().toString())
                .updatedAt(metrics.getUpdatedAt().toString())
                .build();
    }

    private Metrics convertToMetricsEntity(MetricsDTO dto) {
        return modelMapper.map(dto, Metrics.class);
    }
}
