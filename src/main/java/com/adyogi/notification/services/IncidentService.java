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
import com.adyogi.notification.utils.constants.TableConstants;
import com.adyogi.notification.utils.logging.LogUtil;
import com.google.cloud.bigquery.*;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import java.util.*;
import java.util.stream.Collectors;

import static com.adyogi.notification.utils.constants.BigQueryConstants.INCIDENT_QUERY;
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
    private ModelMapper modelMapper;

    @Autowired
    private BigQueryClient bigQueryConfiguration;

    @Autowired
    private IncidentToEmailService incidentToEmailService;

    @Cacheable("validateClientId")
    private void validateClientId(String clientId) {
        if (!clientValidationService.isClientIdValid(clientId)) {
            String errorMessage = String.format(INVALID_CLIENT_ID, clientId);
            logger.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
    }

    private String getStringValue(FieldValueList row, String fieldName) { // Correct parameter type is Row
        if (row == null) {
            return null; // Handle null row
        }

        FieldValue fieldValue = row.get(fieldName);

        if (fieldValue != null && !fieldValue.isNull()) { // Correct null and isNull check
            return fieldValue.getStringValue();
        } else {
            return null; // Return null if fieldValue is null or isNull
        }
    }

    private List<IncidentDTO> fetchResolvedIncidents(String clientId, int LIMIT, int offset) {
        validateClientId(clientId);
        List<IncidentDTO> incidents = new ArrayList<>();
        String query = String.format(INCIDENT_QUERY, clientId) + " LIMIT " + LIMIT + " OFFSET " + offset;

        QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query).build();

        try {
            TableResult results = bigQueryConfiguration.executeQuery(queryConfig);
            for (FieldValueList row : results.iterateAll()) {
                IncidentDTO dto = new IncidentDTO();

                dto.setAlertId(getStringValue(row, ALERT_ID));
                dto.setClientId(getStringValue(row, CLIENT_ID));
                dto.setMetricName(getStringValue(row, METRIC_NAME));
                dto.setMetricName(getStringValue(row, OBJECT_TYPE));
                dto.setObjectId(getStringValue(row, OBJECT_ID));
                dto.setIncidentId(UUID.randomUUID().getMostSignificantBits());
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
                dto.setNotificationSentAT(getStringValue(row, NOTIFICATION_SENT_AT));
                incidents.add(dto);
            }
        } catch (Exception e) {
            logger.error(FAILED_TO_READ_FROM_BIGQUERY, e);
            throw new ServiceException(FAILED_TO_READ_FROM_BIGQUERY, e);
        }

        return incidents;
    }

    public IncidentDTO getIncidentById(String clientId, String idString) {
        validateClientId(clientId);
        try {
            long id = Long.parseLong(idString); // Handle NumberFormatException elsewhere (see below)

            Optional<Incident> optionalIncident = incidentRepository.findByIncidentIdAndClientId(id, clientId);

            return optionalIncident.map(incident -> modelMapper.map(incident, IncidentDTO.class))
                    .orElseThrow(() -> new NotFoundException(INCIDENT_NOT_FOUND)); // Handle not found

        } catch (NumberFormatException e) {  // Handle NumberFormatException
            logger.error(String.format(ERROR_FETCHING_INCIDENT, clientId, idString), e);
            throw new RuntimeException(String.format(ERROR_FETCHING_INCIDENT, clientId, idString), e); // Custom exception
        } catch (Exception e) { // Catch other exceptions
            logger.error(String.format(ERROR_FETCHING_INCIDENT, clientId, idString), e);
            throw new ServiceException(String.format(ERROR_FETCHING_INCIDENT, clientId, idString), e);
        }
    }

    public List<IncidentDTO> getIncidentsByClientId(String clientId, int page, int offset) {
        validateClientId(clientId);
        List<IncidentDTO> openIncidents = getOpenIncident(clientId, page, offset);
        List<IncidentDTO> resolvedIncidents = fetchResolvedIncidents(clientId , page, offset);
        openIncidents.addAll(resolvedIncidents);
        return openIncidents;
    }

    public List<IncidentDTO> getOpenIncident(String clientId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return incidentRepository.findByClientId(clientId, pageable).getContent().stream()
                .map(row -> modelMapper.map(row, IncidentDTO.class))
                .collect(Collectors.toList());
    }

    public void insertMetricToBigQuery(Map<String, Object> incident) {
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

    public void pauseIncidentAlerts(String clientId, String id) {
        validateClientId(clientId);
        try {
            Incident incident = incidentRepository.findByIncidentIdAndClientId(Long.valueOf(id), clientId)
                    .orElseThrow(() -> new NotFoundException(INCIDENT_NOT_FOUND));

            incident.setStatus(TableConstants.STATUS.DISABLED);
            incidentRepository.save(incident);
        }catch (NumberFormatException e) {  // Handle NumberFormatException
            logger.error(String.format(ERROR_FETCHING_INCIDENT, clientId, id), e);
            throw new RuntimeException(String.format(ERROR_FETCHING_INCIDENT, clientId, id), e); // Custom exception
        }
        catch (Exception e) {
            logger.error(String.format(ERROR_PAUSING_ALERTS, clientId, id), e);
            throw new ServiceException(String.format(ERROR_PAUSING_ALERTS, clientId, id), e);
        }
    }

    public void enableIncidentAlert(String clientId, String id) {
        validateClientId(clientId);
        TypeMap<Incident, IncidentDTO> typeMap = modelMapper.createTypeMap(Incident.class, IncidentDTO.class);
        try {
            Incident incident = incidentRepository.findByIncidentIdAndClientId(Long.valueOf(id), clientId)
                    .orElseThrow(() -> new NotFoundException(INCIDENT_NOT_FOUND));


            if (incident.getStatus() != null && incident.getStatus().equals(TableConstants.STATUS.ENABLED)) {
                incident.setStatus(TableConstants.STATUS.ENABLED);
                incidentRepository.save(incident);
            }
        } catch (NumberFormatException e) {  // Handle NumberFormatException
            logger.error(String.format(ERROR_FETCHING_INCIDENT, clientId, id), e);
            throw new RuntimeException(String.format(ERROR_FETCHING_INCIDENT, clientId, id), e); // Custom exception
        }catch (Exception e) {
            logger.error(String.format(ERROR_ENABLING_ALERTS, clientId, id), e);
            throw new ServiceException(String.format(ERROR_ENABLING_ALERTS, clientId, id), e);
        }
    }

    public void insertBaselineToDB(Incident incident) {
        try {
            Baseline baseline = baselineRepository.findBaselineById(incident.getAlertId(),
                    incident.getClientId(),
                    incident.getObjectType(),
                    incident.getObjectId());

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

    public void resolveIncident(String clientId, String id) {
        validateClientId(clientId);
        try {
            Incident incident = incidentRepository.findByIncidentIdAndClientId(Long.valueOf(id), clientId)
                    .orElseThrow(() -> new NotFoundException(INCIDENT_NOT_FOUND));

            incident.setIncidentStatus(TableConstants.INCIDENT_STATUS.RESOLVED);
            incidentRepository.save(incident);

            insertMetricToBigQuery(incident.toMap());
            insertBaselineToDB(incident);
        }catch (NumberFormatException e) {  // Handle NumberFormatException
            logger.error(String.format(ERROR_FETCHING_INCIDENT, clientId, id), e);
            throw new RuntimeException(String.format(ERROR_FETCHING_INCIDENT, clientId, id), e); // Custom exception
        }
        catch (Exception e) {
            logger.error(String.format(INCIDENT_RESOLVE_FAILED, clientId, id), e);
            throw new ServiceException(String.format(INCIDENT_RESOLVE_FAILED, clientId, id), e);
        }
    }

    @Async
    public void triggerIncidentForClient(String clientId) {
        logger.info("Incident started  for  client." + clientId);
        validateClientId(clientId);
        try {
            incidentHandlingService.processIncidentsForClient(clientId);
        }
        catch (Exception e) {
            logger.error(ERROR_PROCESSING_INCIDENT + clientId, e);
//            RollbarManager.sendExceptionOnRollBar(ERROR_PROCESSING_INCIDENT, e);
        }

    }


    @Async
    public void triggerIncidentForAllClients()  {
        // Trigger incident for all clients
        logger.info("Triggering incident for all clients...");
        List <String> clientIds = metricsRepository.getDistinctClientId();

        for (String clientId : clientIds) {
            triggerIncidentForClient(clientId);
        }

        logger.info("Incident completed for all clients.");
    }

    @Async
    public void triggerEmailForClient(String clientId){
        validateClientId(clientId);
        incidentToEmailService.sendEmail(clientId);
    }

    @Async
    public void triggerEmailForAllClient() {
        incidentToEmailService.sendEmailsForAllClient();
        logger.info("Incident completed for all clients.");
    }
}
