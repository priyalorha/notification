package com.adyogi.notification.services;

import com.adyogi.notification.components.AlertEntityDTOMapper;
import com.adyogi.notification.components.TriggerConditionEntityDTOMapper;
import com.adyogi.notification.database.mongo.entities.Alert;
import com.adyogi.notification.database.mongo.entities.TriggerCondition;
import com.adyogi.notification.dto.*;
import com.adyogi.notification.exceptions.NotFoundException;
import com.adyogi.notification.exceptions.ServiceException;
import com.adyogi.notification.repositories.back4app.AlertRepository;
import com.adyogi.notification.retrofits.IAlertRetrofit;
import com.adyogi.notification.retrofits.RetrofitParseInstanceService;
import com.adyogi.notification.utils.constants.ErrorConstants;
import com.adyogi.notification.utils.constants.TableConstants;
import com.adyogi.notification.utils.logging.LogUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.adyogi.notification.utils.constants.ErrorConstants.*;
import static com.adyogi.notification.utils.constants.MongoConstants.ALERT_CHANNEL;
import static com.adyogi.notification.utils.constants.MongoConstants.TRIGGER_CONDITIONS;


@Component
public class AlertService {


    private final ClientValidationService clientValidationService;
    private final AlertRepository repository;
    private final RetrofitParseInstanceService retrofitService;
    private final AlertEntityDTOMapper modelMapper;

    @Autowired
    ObjectMapper objectMapper;

    private final TriggerConditionEntityDTOMapper triggerConditionEntityDTOMapper;

    private static final Logger logger = LogUtil.getInstance();

    private static final Map<TableConstants.METRIC, Class<?>> metricValueTypeMap = new HashMap<>();

    static {
        metricValueTypeMap.put(TableConstants.METRIC.INTEGRATION_FAILURE, StaticBooleanValueDTO.class);
        metricValueTypeMap.put(TableConstants.METRIC.STOPLOSS_LIMIT_REACHED, StaticBooleanValueDTO.class);
        metricValueTypeMap.put(TableConstants.METRIC.MATCH_RATE, StaticFloatValueDTO.class);
        metricValueTypeMap.put(TableConstants.METRIC.STOPLOSS_EXCLUSION_COUNT, StaticIntValueDTO.class);
        metricValueTypeMap.put(TableConstants.METRIC.PRODUCT_SET_COUNT, StaticIntValueDTO.class);
        metricValueTypeMap.put(TableConstants.METRIC.STOPLOSS_EXCLUSION_DATE, DynamicDayValueDTO.class);
        metricValueTypeMap.put(TableConstants.METRIC.STOPLOSS_RAN_DATE, DynamicDayValueDTO.class);

    }

    public AlertService(ClientValidationService clientValidationService,
                              AlertRepository repository,
                              RetrofitParseInstanceService retrofitService,
                              AlertEntityDTOMapper modelMapper,
                        TriggerConditionEntityDTOMapper triggerConditionEntityDTOMapper) {
        this.clientValidationService = clientValidationService;
        this.repository = repository;
        this.retrofitService = retrofitService;
        this.modelMapper = modelMapper;
        this.triggerConditionEntityDTOMapper = triggerConditionEntityDTOMapper;
    }


    private Alert fetchClientNotificationConfiguration(String clientId, String alertId) {
        // Fetch the configuration from the repository
        Alert config = repository.findByObjectId(alertId, clientId);

        if (config == null) {
            String errorMessage = String.format(ErrorConstants.ALERT_NOT_FOUND_MESSAGE, alertId, clientId);
            logger.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        return config;
    }

    public AlertDTO getAlerts(String clientId, String alertId) {
        clientValidationService.validateClientId(clientId);
        return modelMapper.convertEntityToDTO(fetchClientNotificationConfiguration(clientId, alertId));
    }

    public List<AlertDTO> getAlerts(String clientId) {
        clientValidationService.validateClientId(clientId);
        return repository.findByClientId(clientId)
                .stream()
                .map(modelMapper::convertEntityToDTO)
                .collect(Collectors.toList());
    }

    public void validateAlert(AlertDTO alertDTO) {
        for (TriggerConditionDTO triggerCondition : alertDTO.getTriggerConditions()){
            Class<?> expectedType = metricValueTypeMap.get(triggerCondition.getMetric());

            if (expectedType != null && !expectedType.isInstance(triggerCondition.getValue())) {
                throw new ServiceException(String.format(INVALID_VALUE_TYPE,
                        expectedType.getSimpleName(), triggerCondition.getMetric()));
            }
        }
    }



    public AlertDTO createAlert(String clientId,
                                            AlertDTO configuration) throws IOException {
        clientValidationService.validateClientId(clientId);
        configuration.setClientId(clientId);
        validateAlert(configuration);

        Alert configEntity = modelMapper.convertDTOToEntity(configuration);
        Retrofit retrofitInstance = retrofitService.getRetrofitInstanceWithJacksonConverter();
        IAlertRetrofit retrofitApi = retrofitInstance.create(IAlertRetrofit.class);

        Response<Alert> response = retrofitApi.saveConfiguration(configEntity).execute();
        if (response.isSuccessful()) {
            configuration.setObjectId(String.valueOf(response.body().getObjectId()));
            configuration.setCreatedAt(response.body().getCreatedAt());
            return configuration;
        }

        throw new RuntimeException(String.valueOf(response.errorBody()));
    }


    public void verifyClientAndObjectIdsUnchanged(String clientId, String objectId, AlertDTO alertDTO){
        if (alertDTO.getClientId() != null && !clientId.equals(alertDTO.getClientId())) {
            throw new ServiceException(String.format(CANNOT_UPDATE_CLIENT_ID,
                    clientId, alertDTO.getClientId()));
        }

        if (alertDTO.getObjectId() != null && !objectId.equals(alertDTO.getObjectId())) {
            throw new ServiceException(String.format(CANNOT_UPDATE_ALERT_ID,
                    objectId, alertDTO.getObjectId()));
        }

    }

    public AlertDTO updateAlert(String clientId,
                                            String alertId,
                                            AlertDTO configuration) {
        clientValidationService.validateClientId(clientId);

        verifyClientAndObjectIdsUnchanged(clientId, alertId, configuration);

        Alert existingConfig = fetchClientNotificationConfiguration(clientId, alertId);
        Alert updatedConfig = modelMapper.convertDTOToEntity(configuration);
        updatedConfig.setObjectId(existingConfig.getObjectId());
        updatedConfig.setClientId(existingConfig.getClientId());

        updatedConfig.setUpdatedAt(Date.from(Instant.now()));


        return modelMapper.convertEntityToDTO(repository.save(updatedConfig));
    }

    public AlertDTO patchAlert(String clientId,
                                String alertId,
                                AlertDTO updatedDTO) {
        clientValidationService.validateClientId(clientId);

        verifyClientAndObjectIdsUnchanged(clientId, alertId, updatedDTO);

        try {

            Alert existingAlert = fetchClientNotificationConfiguration(clientId, alertId);

            JsonNode updatedJsonNode = objectMapper.valueToTree(updatedDTO);
            ObjectNode updatedObjectNode = (ObjectNode) updatedJsonNode;

            updatedObjectNode.remove(TRIGGER_CONDITIONS);
            updatedObjectNode.remove(ALERT_CHANNEL);

            JsonMergePatch patch = JsonMergePatch.fromJson(updatedObjectNode);
            JsonNode patchedJsonNode = patch.apply(objectMapper.valueToTree(existingAlert));

            Alert updatedAlert = objectMapper.treeToValue(patchedJsonNode, Alert.class);

            if (updatedDTO.getTriggerConditions()!=null)
            {
                List<TriggerCondition> triggerConditions = updatedDTO.getTriggerConditions()
                        .stream()
                        .map(client -> {
                            try {
                                return triggerConditionEntityDTOMapper.convertDTOToEntity(client);
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .collect(Collectors.toList());
                updatedAlert.setTriggerConditions(triggerConditions);
            }


            if (updatedDTO.getAlertChannel()!=null)
            {
                updatedAlert.setAlertChannel(updatedDTO.getAlertChannel());
            }

            updatedAlert.setUpdatedAt(Date.from(Instant.now()));

            updatedAlert = repository.save(updatedAlert);

            return modelMapper.convertEntityToDTO(updatedAlert);
        }
        catch (JsonProcessingException e) {
            logger.error("JSON processing error while updating alert: {}", e.getMessage(), e);
            throw new ServiceException(String.format(ERROR_UPDATING_ALERTING, clientId, alertId), e);
        }
        catch (Exception e) {
            logger.error(String.format(ERROR_UPDATING_ALERTING, clientId, alertId), e);
            throw new ServiceException(String.format(ERROR_UPDATING_ALERTING, clientId, alertId)+e.getMessage());
        }
    }


    public void deleteAlert(String clientId, String alertId) {
        clientValidationService.validateClientId(clientId);
        Alert config = fetchClientNotificationConfiguration(clientId, alertId);
        repository.delete(config);
    }
}
