package com.adyogi.notification.services;

import com.adyogi.notification.bigquery.bigquerycomponent.BigQueryClient;
import com.adyogi.notification.database.sql.entities.Baseline;
import com.adyogi.notification.database.sql.entities.Incident;
import com.adyogi.notification.dto.IncidentDTO;
import com.adyogi.notification.exceptions.NotFoundException;
import com.adyogi.notification.exceptions.ServiceException;
import com.adyogi.notification.repositories.mysql.BaselineRepository;
import com.adyogi.notification.repositories.mysql.IncidentRepository;
import com.adyogi.notification.repositories.mysql.MetricsRepository;
import com.adyogi.notification.utils.FailureHandler;
import com.adyogi.notification.utils.constants.TableConstants;
import com.adyogi.notification.utils.logging.LogUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.bigquery.*;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.adyogi.notification.utils.constants.AlertConstants.INCIDENT_COMPUTE_TOPIC;
import static com.adyogi.notification.utils.constants.BigQueryConstants.INCIDENT_QUERY;
import static com.adyogi.notification.utils.constants.ConfigConstants.*;
import static com.adyogi.notification.utils.constants.ErrorConstants.*;
import static com.adyogi.notification.utils.constants.RequestDTOConstants.*;
import static com.adyogi.notification.utils.constants.TableConstants.BIGQUERY_INCIDENTS_TABLE_NAME;
import static com.adyogi.notification.utils.constants.TableConstants.BIGQUERY_NOTIFICATION_DATASET_NAME;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private static final Logger logger = LogUtil.getInstance();

    @Autowired
    IncidentRepository incidentRepository;
    @Autowired
    ClientValidationService clientValidationService;
    @Autowired
    BaselineRepository baselineRepository;
    @Autowired
    MetricsRepository metricsRepository;

    @Autowired
    IncidentHandlingService incidentHandlingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private BigQueryClient bigQueryConfiguration;

    @Autowired
    private IncidentToEmailService incidentToEmailService;

    @Autowired
    private RateLimiterService rateLimiterService;

    @Autowired
    PubSubPublisher pubSubPublisher;

    @Autowired
    FailureHandler failureHandler;

    private String getStringValue(FieldValueList row, String fieldName) { // Correct parameter type is Row
        if (row == null) {
            return null; // Handle null row
        }

        FieldValue fieldValue = row.get(fieldName);

        if (fieldValue != null && !fieldValue.isNull()) { // Correct null and isNull check
            return fieldValue.getStringValue().toUpperCase(Locale.ROOT);
        } else {
            return "0"; // Return null if fieldValue is null or isNull
        }
    }

    private List<IncidentDTO> fetchResolvedIncidents(String clientId, int page, int limit) {


        clientValidationService.validateClientId(clientId);
        List<IncidentDTO> incidents = new ArrayList<>();

        String query = String.format(INCIDENT_QUERY, clientId) + " LIMIT " + limit + " OFFSET " + (page-1) * limit;

        QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query).build();

        try {
            TableResult results = bigQueryConfiguration.executeQuery(queryConfig);
            for (FieldValueList row : results.iterateAll()) {
                IncidentDTO dto = new IncidentDTO();

                dto.setAlertId(getStringValue(row, ALERT_ID));
                dto.setClientId(getStringValue(row, CLIENT_ID));
                dto.setMetric(getStringValue(row, METRIC));
                dto.setMetric(getStringValue(row, OBJECT_TYPE));
                dto.setObjectIdentifier(getStringValue(row, OBJECT_IDENTIFIER));
                dto.setIncidentId(Long.parseLong(getStringValue(row, INCIDENT_ID)));
                dto.setStatus(getStringValue(row, STATUS));
                dto.setMessage(getStringValue(row, MESSAGE));
                dto.setNotificationStatus(getStringValue(row, NOTIFICATION_STATUS));
                dto.setIncidentStatus(getStringValue(row, INCIDENT_STATUS));
                dto.setBaseValue(getStringValue(row, BASE_VALUE));
                dto.setValue(getStringValue(row, VALUE));
                dto.setValueDataType(getStringValue(row, VALUE_DATA_TYPE));
                dto.setCreatedAt(getStringValue(row, CREATED_AT));
                dto.setUpdatedAt(getStringValue(row, UPDATED_AT));
                dto.setAlertResendIntervalMin(getStringValue(row, ALERT_RESEND_INTERVAL_MIN));
                dto.setAlertChannel(getStringValue(row, ALERT_CHANNEL_FIELD_NAME));
                dto.setNotificationSentAt(getStringValue(row, NOTIFICATION_SENT_AT));
                incidents.add(dto);
            }
        } catch (Exception e) {
            logger.error(FAILED_TO_READ_FROM_BIGQUERY, e);
            throw new ServiceException(FAILED_TO_READ_FROM_BIGQUERY, e);
        }

        return incidents;
    }

    public IncidentDTO getIncidentById(String clientId, String idString) {
        clientValidationService.validateClientId(clientId);
        try {
            long id = Long.parseLong(idString); // Handle NumberFormatException elsewhere (see below)

            Optional<Incident> optionalIncident = incidentRepository.findByIncidentIdAndClientId(id, clientId);

            return optionalIncident.map(incident -> modelMapper.map(incident, IncidentDTO.class))
                    .orElseThrow(() -> new NotFoundException(INCIDENT_NOT_FOUND));
        }
         catch (NumberFormatException e) {  // Handle NumberFormatException
            logger.error(String.format(ERROR_FETCHING_INCIDENT, clientId, idString), e);
            throw new ServiceException(String.format(ERROR_FETCHING_INCIDENT, clientId, idString), e); // Custom exception
        }
        catch (Exception e) { // Catch other exceptions
            logger.error(String.format(ERROR_FETCHING_INCIDENT, clientId, idString), e);
            throw e;
        }
    }

    public List<IncidentDTO> getIncidentsByClientId(String clientId, int page, int offset) {
        clientValidationService.validateClientId(clientId);
        List<IncidentDTO> openIncidents = getOpenIncident(clientId, page, offset);
//        List<IncidentDTO> resolvedIncidents = fetchResolvedIncidents(clientId , page, offset);
        return openIncidents;
    }

    public List<IncidentDTO> getOpenIncident(String clientId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return incidentRepository.findByClientId(clientId, pageable).getContent().stream()
                .map(row -> modelMapper.map(row, IncidentDTO.class))
                .collect(Collectors.toList());
    }

    public void insertIncidentToBigQuery(Map<String, Object> incident) {
        try {
            InsertAllRequest.RowToInsert rowToInsert = InsertAllRequest.RowToInsert.of(incident);
            bigQueryConfiguration.insertRows(
                    Collections.singletonList(rowToInsert),
                    BIGQUERY_NOTIFICATION_DATASET_NAME,
                    BIGQUERY_INCIDENTS_TABLE_NAME
            );
        } catch (Exception e) {
            logger.error(ERROR_INSERTING_BASELINE_TO_BQ, e);
            throw new ServiceException(ERROR_INSERTING_BASELINE_TO_BQ, e);
        }
    }

    public void verifyClientAndObjectIdsUnchanged(String clientId, String incidentId, IncidentDTO dto){
        if (dto.getClientId() != null && !clientId.equals(dto.getClientId())) {
            throw new ServiceException(String.format(CANNOT_UPDATE_CLIENT_ID,
                    clientId, dto.getClientId()));
        }

        if (dto.getIncidentId() != null && !incidentId.equals(dto.getIncidentId()) ) {
            throw new ServiceException(String.format(CANNOT_UPDATE_ALERT_ID,
                    incidentId, dto.getObjectIdentifier()));
        }

    }


    public void updateBaseline(Incident incident) {
        try {
            Baseline baseline = baselineRepository.findBaselineById(incident.getAlertId(),
                    incident.getClientId(),
                    incident.getObjectType(),
                    incident.getObjectIdentifier());

            if (baseline != null) {
                baseline.setValue(incident.getValue());
                baseline.setUpdatedAt(incident.getUpdatedAt());
                baselineRepository.save(baseline);
            } else {
                logger.error(String.format(BASELINE_NOT_FOUND_FOR_INCIDENT, incident.getClientId(), incident.getIncidentId()));
                throw new ServiceException(String.format(BASELINE_NOT_FOUND_FOR_INCIDENT, incident.getClientId(), incident.getIncidentId()));
            }
        } catch (Exception e) {
            logger.error(ERROR_INSERTING_BASELINE_TO_BQ, e);
            throw new ServiceException(ERROR_INSERTING_BASELINE_TO_BQ, e);
        }
    }

    public IncidentDTO patchIncident(String clientId, String id, IncidentDTO incidentDTO){
        clientValidationService.validateClientId(clientId);
        try {
            Long incidentId = Long.valueOf(id);
            verifyClientAndObjectIdsUnchanged(clientId, id, incidentDTO);

            Incident incident = incidentRepository.findByIncidentIdAndClientId(incidentId, clientId)
                    .orElseThrow(() ->  new NotFoundException(String.format(INCIDENT_NOT_FOUND, clientId, id)));

            if (incidentDTO.getStatus() != null) {
                incident.setStatus(TableConstants.ALERT_STATUS.valueOf(incidentDTO.getStatus()));
            }

            if (incidentDTO.getIncidentStatus()!= null) {
                if(incident.getIncidentStatus().equals(TableConstants.INCIDENT_STATUS.RESOLVED))
                {
                    throw new ServiceException(String.format(INCIDENT_ALREADY_RESOLVED, clientId , id));
                }
                incident.setIncidentStatus(TableConstants.INCIDENT_STATUS.valueOf(incidentDTO.getIncidentStatus()));

                if (incidentDTO.getIncidentStatus().equals(TableConstants.INCIDENT_STATUS.RESOLVED.toString())) {
                    insertIncidentToBigQuery(incident.toMap());
                    updateBaseline(incident);
                }
            }

            incident.setUpdatedAt(LocalDateTime.from(Instant.now()));

            return modelMapper.map( incidentRepository.save(incident), IncidentDTO.class);

        } catch (NumberFormatException e) {  // Handle NumberFormatException
            logger.error(String.format(INVALID_INCIDENT_ID, id, clientId), e);
            throw new ServiceException(String.format(INVALID_INCIDENT_ID, id, clientId), e); // Custom exception
        }catch(NotFoundException e) {
            logger.error(String.format(INCIDENT_NOT_FOUND, clientId, id), e);
            throw e;
        }
        catch (Exception e) {
            logger.error(String.format(ERROR_ENABLING_ALERTS, clientId, id), e);
            throw new ServiceException(String.format(ERROR_ENABLING_ALERTS, clientId, id), e);
        }


    }


    public void triggerIncidentForClient(String clientId) throws IOException {
        logger.info("Incident started  for  client." + clientId);

        clientValidationService.validateClientId(clientId);

        if (!rateLimiterService.canTrigger(INCIDENT_COMPUTE_TOPIC,
                clientId)) {
            logger.info("Skipping incident for clientId: {} (sent within the last 5 minutes)", clientId);
            throw new ServiceException(INCIDENT_TRIGGERED_RECENTLY);
        }

        try {
            pubSubPublisher.publishToPubSub(INCIDENT_COMPUTE_TOPIC, clientId);
        }
        catch (Exception e) {
            logger.error(ERROR_PROCESSING_INCIDENT + clientId, e);
            failureHandler.handleFailure(ERROR_PROCESSING_INCIDENT + clientId, e);
        }
    }


    public void triggerIncidentForAllClients()  {
        // Trigger incident for all clients
        logger.info("Triggering incident for all clients...");

        if (!rateLimiterService.canTrigger(INCIDENT_COMPUTE_TOPIC,
                ALL_CLIENT_INCIDENT)) {
            logger.info("Skipping incident for clientId: {} (sent within the last 5 minutes)", ALL_CLIENT_INCIDENT);
            throw new ServiceException(INCIDENT_TRIGGERED_RECENTLY);
        }

        List <String> clientIds = metricsRepository.getDistinctClientId();

        for (String clientId : clientIds) {
            pubSubPublisher.publishToPubSub(INCIDENT_COMPUTE_TOPIC, clientId);
        }

        logger.info("Incident completed for all clients.");
    }


    public void triggerEmailForClient(String clientId){
        clientValidationService.validateClientId(clientId);
        if (!rateLimiterService.canTrigger(EMAIL_CACHE,
                clientId)) {
            logger.info("Skipping email for clientId: {} (sent within the last 5 minutes)", clientId);
            throw new ServiceException(EMAIL_SENT_RECENTLY);
        }
        incidentToEmailService.sendEmail(clientId);
    }

    public void triggerEmailForAllClient() {
        if (!rateLimiterService.canTrigger(EMAIL_CACHE,
                ALL_CLIENT_EMAIL)) {
            logger.info("Skipping email for clientId: {} (sent within the last 5 minutes)", ALL_CLIENT_EMAIL);
            throw new ServiceException(EMAIL_SENT_RECENTLY);
        }
        incidentToEmailService.sendEmailsForAllClient();
        logger.info("Incident completed for all clients.");
    }
}
