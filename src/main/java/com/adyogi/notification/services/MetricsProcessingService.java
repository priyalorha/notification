package com.adyogi.notification.services;

import com.adyogi.notification.database.sql.entities.Metrics;
import com.adyogi.notification.dto.MetricsDTO;
import com.adyogi.notification.exceptions.NotFoundException;
import com.adyogi.notification.exceptions.ServiceException;
import com.adyogi.notification.utils.constants.TableConstants;
import com.adyogi.notification.utils.logging.LogUtil;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.adyogi.notification.utils.constants.ConfigConstants.BULK_SAVE_METRICS_TASK;
import static com.adyogi.notification.utils.constants.ErrorConstants.*;
import static com.adyogi.notification.utils.constants.ValidationConstants.*;

@Service
public class MetricsProcessingService {

    private static final Logger logger = LogUtil.getInstance();

    private final ClientValidationService clientValidationService;
    private final MetricsService metricsService;
    private final ModelMapper modelMapper;

    private static final Map< TableConstants.METRIC, String> METRIC_TYPES = new HashMap<>();
    static {
        METRIC_TYPES.put(TableConstants.METRIC.INTEGRATION_FAILURE, BOOLEAN_DATA_TYPE);
        METRIC_TYPES.put(TableConstants.METRIC.MATCH_RATE, FLOAT_DATA_TYPE);
        METRIC_TYPES.put(TableConstants.METRIC.STOPLOSS_LIMIT_REACHED, BOOLEAN_DATA_TYPE);
        METRIC_TYPES.put(TableConstants.METRIC.STOPLOSS_EXCLUSION_COUNT, INTEGER_DATA_TYPE);
        METRIC_TYPES.put(TableConstants.METRIC.STOPLOSS_EXCLUSION_DATE, DATE_DATA_TYPE);
        METRIC_TYPES.put(TableConstants.METRIC.PRODUCT_SET_COUNT, INTEGER_DATA_TYPE);
        METRIC_TYPES.put(TableConstants.METRIC.STOPLOSS_RAN_DATE, DATE_DATA_TYPE);
    }

    public MetricsProcessingService(ClientValidationService clientValidationService,
                                    MetricsService metricsService,
                                    ModelMapper modelMapper) {
        this.clientValidationService = clientValidationService;
        this.metricsService = metricsService;
        this.modelMapper = modelMapper;
    }


    public boolean validateMetric(MetricsDTO metricRequest) {
        if (!Objects.equals(METRIC_TYPES.get(metricRequest.getMetric()), metricRequest.getValueDataType())) {
            throw new ServiceException(String.format(INVALID_METRIC_COMBINATION));
        }
        String value = metricRequest.getValue();

            switch (metricRequest.getMetric()) {
                case MATCH_RATE:
                    Float.parseFloat(value);
                    return true;
                case STOPLOSS_LIMIT_REACHED:
                case INTEGRATION_FAILURE:
                    if (!TRUE_VALUE.equalsIgnoreCase(value) && !FALSE_VALUE.equalsIgnoreCase(value)) {
                        throw new IllegalArgumentException("Invalid value: " + value + ". Expected: " + TRUE_VALUE + " or " + FALSE_VALUE);
                    }
                    return true;

                case STOPLOSS_EXCLUSION_COUNT:
                case PRODUCT_SET_COUNT:
                    Integer.parseInt(value);
                    return true;
                case STOPLOSS_EXCLUSION_DATE:
                case STOPLOSS_RAN_DATE:
                    LocalDateTime localDateTime = Instant.parse(value)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDateTime();

                    return true;
                default:
                    logger.error("Unexpected metric type: " + metricRequest.getMetric());
                    return false;
            }

    }

    public MetricsDTO saveMetrics(String clientId, MetricsDTO metricRequest) {
        clientValidationService.validateClientId( clientId);

        validateMetric(metricRequest);
        try {
            Metrics savedMetric = metricsService.saveMetrics(clientId, metricRequest);
            MetricsDTO MetricsDTO = convertToDTO(savedMetric);
            metricsService.insertMetricToBigQuery(MetricsDTO);
            return MetricsDTO;
        }
        catch (Exception e) {
            logger.error(String.format("SOME ERROR  ", metricRequest), e);
            throw new ServiceException(String.format(ERROR_SAVING_METRIC, metricRequest));
        }
    }



    private boolean isValidMetric(MetricsDTO metric) {
        try {
            boolean isValid = clientValidationService.isClientIdValid(metric.getClientId()) && validateMetric(metric);
            if (!isValid) {
                logger.warn("Metric validation failed for clientId: {}, metric: {}, value: {}",
                        metric.getClientId(), metric.getMetric(), metric.getValue());
            }
            return isValid;
        } catch (Exception e) {
            logger.error("Validation error for clientId: {}, metric: {}, value: {}",
                    metric.getClientId(), metric.getMetric(), metric.getValue(), e);
            return false;
        }
    }

    public List<MetricsDTO> bulkMetricsSave(List<MetricsDTO> metricsRequest) {
        Map<Boolean, List<MetricsDTO>> partitionedMetrics = metricsRequest.stream()
                .collect(Collectors.partitioningBy(metric -> {
                    try {
                        return isValidMetric(metric);
                    } catch (Exception e) {
                        logger.error("Validation failed for metric: " + metric.getMetric() +
                                " with value: " + metric.getValue() + " Client ID: " + metric.getClientId(), e);
                        return false;
                    }
                }));
        List<MetricsDTO> validMetrics = partitionedMetrics.get(true);

        List<MetricsDTO> invalidMetrics = partitionedMetrics.get(false);

        if(!validMetrics.isEmpty()) {
            logger.info("No valid metrics found for bulk processing...");
            bulkSaveWithoutClientId(validMetrics);
        }

        return invalidMetrics;
    }



    @Async(BULK_SAVE_METRICS_TASK)
    public void bulkSaveWithoutClientId(List<MetricsDTO> metricsRequest) {
        logger.info("Starting bulk processing of metrics...");

        if(metricsRequest.isEmpty()) {
            logger.info("No valid metrics found for bulk processing...");
            return ;
        }

        List<Metrics> metrics = metricsRequest.stream().map(this::convertToMetricsEntity)
                .collect(Collectors.toList());

        List<MetricsDTO> bigQueryMetrics = metricsRequest.stream()
                .map(dto -> {
                    MetricsDTO bigQueryMetric = modelMapper.map(dto, MetricsDTO.class);
                    bigQueryMetric.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
                    bigQueryMetric.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
                    return bigQueryMetric;
                })
                .collect(Collectors.toList());



        metricsService.bulkUpsertMetrics(metrics);
        metricsService.insertMetricsToBigQuery(bigQueryMetrics);
        logger.info("Completed metrics bulk processing...");
    }

    private MetricsDTO convertToDTO(Metrics metrics) {
        return modelMapper.map(metrics, MetricsDTO.class);
    }

    private Metrics convertToMetricsEntity(MetricsDTO dto) {
        return modelMapper.map(dto, Metrics.class);
    }
}
