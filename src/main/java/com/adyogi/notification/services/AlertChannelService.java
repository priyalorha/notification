package com.adyogi.notification.services;

import com.adyogi.notification.database.mongo.entities.AlertChannel;
import com.adyogi.notification.dto.AlertChannelDTO;
import com.adyogi.notification.exceptions.ClientValidationException;
import com.adyogi.notification.exceptions.NotFoundException;
import com.adyogi.notification.exceptions.ServiceException;
import com.adyogi.notification.repositories.back4app.AlertChannelRepository;
import com.adyogi.notification.retrofits.IAlertChannelRetrofit;
import com.adyogi.notification.retrofits.RetrofitParseInstanceService;
import com.adyogi.notification.utils.logging.LogUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.adyogi.notification.utils.constants.ErrorConstants.*;

@Service
@RequiredArgsConstructor
public class AlertChannelService {

    private static final Logger logger = LogUtil.getInstance();

    @Autowired
    private final ClientValidationService clientValidationService;
    @Autowired
    private final AlertChannelRepository alertChannelRepository;
    @Autowired
    private final ModelMapper modelMapper;
    @Autowired
    private final RetrofitParseInstanceService retrofitService;

    @Autowired
    ObjectMapper objectMapper;

    public AlertChannelDTO saveAlertChannel(AlertChannelDTO dto) throws IOException {
        AlertChannel entity = modelMapper.map(dto, AlertChannel.class);
        Response<AlertChannel> response = postAlertChannel(entity);
        return processSaveResponse(entity, response);
    }

    private Response<AlertChannel> postAlertChannel(AlertChannel entity) throws IOException {
        Retrofit retrofitInstance = retrofitService.getRetrofitInstanceWithJacksonConverter();
        IAlertChannelRetrofit retrofitApi = retrofitInstance.create(IAlertChannelRetrofit.class);
        return retrofitApi.postAlertChannel(entity).execute();
    }

    private AlertChannelDTO processSaveResponse(AlertChannel entity, Response<AlertChannel> response) {
        if (response.isSuccessful() && response.body() != null) {
            entity.setObjectId(String.valueOf(response.body().getObjectId()));
            entity.setCreatedAt(response.body().getCreatedAt());
            return modelMapper.map(entity, AlertChannelDTO.class);
        }
        throw new RuntimeException(String.valueOf(response.errorBody()));
    }

    public AlertChannelDTO storeAlertChannel(String clientId, AlertChannelDTO dto) throws IOException {
        clientValidationService.validateClientId(clientId);
        dto.setClientId(clientId);
        checkChannelExists(clientId, dto);
        return saveAlertChannel(dto);
    }


    private void checkChannelExists(String clientId, AlertChannelDTO dto) {
        if (alertChannelRepository.findByClientIdAndAlertChannel(clientId, String.valueOf(dto.getAlertChannel())) != null) {
            String errorMessage = String.format(CHANNEL_ALREADY_EXISTS, clientId);
            logger.error(errorMessage);
            throw new ClientValidationException(errorMessage);
        }
    }

    public List<AlertChannelDTO> getAllAlertChannels(String clientId) {
        clientValidationService.validateClientId(clientId);
        return alertChannelRepository.findByClientId(clientId)
                .stream()
                .map(client -> modelMapper.map(client, AlertChannelDTO.class))
                .collect(Collectors.toList());
    }

    public void verifyClientAndObjectIdsUnchanged(String clientId, String objectId, AlertChannelDTO dto){
        if (dto.getClientId() != null && !clientId.equals(dto.getClientId())) {
            throw new ServiceException(String.format(CANNOT_UPDATE_CLIENT_ID,
                    clientId, dto.getClientId()));
        }

        if (dto.getObjectId() != null && !objectId.equals(dto.getObjectId())) {
            throw new ServiceException(String.format(CANNOT_UPDATE_ALERT_ID,
                    objectId, dto.getObjectId()));
        }


    }

    public AlertChannelDTO updateAlertChannel(String clientId, String objectId, AlertChannelDTO dto) throws IOException {
        clientValidationService.validateClientId(clientId);
        verifyClientAndObjectIdsUnchanged(clientId, objectId, dto);
        AlertChannel existingChannel = findExistingChannel(clientId, objectId);
        if (dto.getAlertChannel() != existingChannel.getAlertChannel()) {
            throw new ServiceException(CANNOT_UPDATE_CHANNEL);
        }

        AlertChannel temp = modelMapper.map(dto, AlertChannel.class);
        temp.setUpdatedAt(Date.from(Instant.now()));
        return modelMapper.map(
                alertChannelRepository.save(temp),
                AlertChannelDTO.class);
    }

    public AlertChannelDTO getAlertChannel(String clientId, String objectId) {
        clientValidationService.validateClientId(clientId);
        return modelMapper.map(findExistingChannel(clientId, objectId), AlertChannelDTO.class);
    }

    private AlertChannel findExistingChannel(String clientId, String objectId) {
        AlertChannel channel = alertChannelRepository.findById(objectId)
                .orElseThrow(() -> {
                    String errorMessage = String.format(CHANNEL_NOT_FOUND, objectId, clientId);
                    logger.error(errorMessage);
                    return new NotFoundException(errorMessage);
                });

        if (!channel.getClientId().equals(clientId)) {
            String errorMessage = String.format(CHANNEL_NOT_FOUND, objectId, clientId);
            logger.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }

        return channel;
    }

    public void deleteAlertChannel(String clientId, String objectId) {
        clientValidationService.validateClientId(clientId);
        AlertChannel channelToDelete = findExistingChannel(clientId, objectId);
        alertChannelRepository.delete(channelToDelete);
    }

    public AlertChannelDTO patchAlertChannel(String clientId,
                               String objectId,
                               AlertChannelDTO updatedDTO) {

        clientValidationService.validateClientId(clientId);
        verifyClientAndObjectIdsUnchanged(clientId, objectId, updatedDTO);

        try {
            AlertChannel existingChannel = findExistingChannel(clientId, objectId);

            if (updatedDTO.getAlertChannel() != existingChannel.getAlertChannel()) {
                throw new ServiceException(CANNOT_UPDATE_CHANNEL);
            }

            JsonNode existingJson = objectMapper.valueToTree(existingChannel);
            JsonNode updateJson = objectMapper.valueToTree(updatedDTO);

            // Apply the JSON Merge Patch
            JsonMergePatch patch = JsonMergePatch.fromJson(updateJson);
            JsonNode patchedJsonNode = patch.apply(existingJson);

            AlertChannel updatedAlert = objectMapper.treeToValue(patchedJsonNode, AlertChannel.class);

            updatedAlert.setUpdatedAt(Date.from(Instant.now()));

            updatedAlert = alertChannelRepository.save(updatedAlert);

            return modelMapper.map(updatedAlert, AlertChannelDTO.class);
        }
        catch (Exception e) {
            logger.error(String.format(ERROR_UPDATING_ALERTING, clientId, objectId), e);
            throw new ServiceException(String.format(ERROR_UPDATING_ALERTING, clientId, objectId)+e.getMessage());
        }
    }

}
