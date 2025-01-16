package com.adyogi.notification.controller;

import com.adyogi.notification.dto.BigQueryMetricsDTO;
import com.adyogi.notification.dto.MetricsDTO;
import com.adyogi.notification.services.MetricsProcessingService;
import com.adyogi.notification.utils.constants.ConfigConstants;
import com.adyogi.notification.utils.logging.annotation.MDCValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

import static com.adyogi.notification.utils.constants.RequestDTOConstants.CLIENT_ID;


@RestController
@RequestMapping("/clients")
@Validated
public class MetricsController {

    @Autowired
    private MetricsProcessingService metricsProcessingService;

    @PostMapping("/{client_id}/metrics")
    public ResponseEntity<BigQueryMetricsDTO> createOrUpdateMetrics(
            @MDCValue(ConfigConstants.CLIENT_ID_LOGGING_FIELD_NAME)
            @PathVariable(CLIENT_ID) @NotBlank String clientId,
            @Valid @RequestBody MetricsDTO metricRequest)  {

        // Return response with BigQueryMetrics
        return new ResponseEntity<>( metricsProcessingService.saveMetrics(clientId, metricRequest),
                HttpStatus.CREATED);
    }

    @PostMapping("/metrics")
    public ResponseEntity<Object> createOrUpdateMetricsBulk(
            @MDCValue(ConfigConstants.BULK_METRICS_LOGGING_FIELD_NAME)
            @Valid @RequestBody List<MetricsDTO> metricsRequest)  {

        metricsProcessingService.BulkSaveWithoutClientId(metricsRequest);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);

    }
}
