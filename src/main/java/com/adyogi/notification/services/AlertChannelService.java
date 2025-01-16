package com.adyogi.notification.services;

import com.adyogi.notification.database.mongo.entities.AlertChannel;
import com.adyogi.notification.dto.AlertChannelDTO;
import com.adyogi.notification.exceptions.ClientValidationException;
import com.adyogi.notification.exceptions.NotFoundException;
import com.adyogi.notification.repositories.back4app.AlertChannelRepository;
import com.adyogi.notification.retrofits.IAlertChannelRetrofit;
import com.adyogi.notification.retrofits.RetrofitParseInstanceService;
import com.adyogi.notification.utils.logging.LogUtil;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.adyogi.notification.utils.constants.ErrorConstants.*;

@Service
@RequiredArgsConstructor
public class AlertChannelService {

    private static final Logger logger = LogUtil.getInstance();

    private final ClientValidationService clientValidationService;
    private final AlertChannelRepository alertChannelRepository;
    private final ModelMapper modelMapper;

    @Autowired
    private final RetrofitParseInstanceService retrofitService;

    public AlertChannelDTO saveAlertChannel(AlertChannelDTO dto) throws IOException {
        AlertChannel entity = modelMapper.map(dto, AlertChannel.class);
        Response<AlertChannel> response = postAlertChannel(entity);
        return modelMapper.map(processSaveResponse(entity, response), AlertChannelDTO.class);
    }

    private Response<AlertChannel> postAlertChannel(AlertChannel entity) throws IOException {
        Retrofit retrofitInstance = retrofitService.getRetrofitInstanceWithJacksonConverter();
        IAlertChannelRetrofit retrofitApi = retrofitInstance.create(IAlertChannelRetrofit.class);
        return retrofitApi.postAlertChannel(entity).execute();
    }

    private AlertChannel processSaveResponse(AlertChannel entity, Response<AlertChannel> response) {
        if (response.isSuccessful() && response.body() != null) {
            entity.setObjectId(String.valueOf(response.body().getObjectId()));
            entity.setCreatedAt(response.body().getCreatedAt());
            return entity;
        }
        throw new RuntimeException(String.valueOf(response.errorBody()));
    }


    @Cacheable("validateClientId")
    private void validateClientId(String clientId) {
        if (!clientValidationService.isClientIdValid(clientId)) {
            String errorMessage = String.format(INVALID_CLIENT_ID, clientId);
            logger.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
    }

    public AlertChannelDTO storeCommunicationChannel(String clientId, AlertChannelDTO dto) throws IOException {
        validateClientId(clientId);
        dto.setClientId(clientId);
        checkChannelExists(clientId, dto);
        return modelMapper.map(saveAlertChannel(dto), AlertChannelDTO.class);
    }


    private void checkChannelExists(String clientId, AlertChannelDTO dto) {
        if (alertChannelRepository.findByClientIdAndAlertType(clientId, String.valueOf(dto.getAlertChannel())) != null) {
            String errorMessage = String.format(CHANNEL_ALREADY_EXISTS, clientId);
            logger.error(errorMessage);
            throw new ClientValidationException(errorMessage);
        }
    }

    public List<AlertChannelDTO> getAllCommunicationChannels(String clientId) {
        validateClientId(clientId);
        return alertChannelRepository.findByClientId(clientId)
                .stream()
                .map(client -> modelMapper.map(client, AlertChannelDTO.class))
                .collect(Collectors.toList());
    }

    public AlertChannelDTO updateCommunicationChannel(String clientId, String objectId, AlertChannelDTO dto) throws IOException {
        validateClientId(clientId);
        AlertChannel existingChannel = findExistingChannel(clientId, objectId);
        return modelMapper.map(
                alertChannelRepository.save(updateChannelFields(existingChannel, dto)),
                AlertChannelDTO.class);
    }

    public AlertChannelDTO getCommunicationChannel(String clientId, String objectId) {
        validateClientId(clientId);
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

    private AlertChannel updateChannelFields(AlertChannel existingChannel, AlertChannelDTO dto) {

        if (dto.getCommunicationConfiguration() != null) {
            AlertChannel temp = modelMapper.map(dto, AlertChannel.class);
            existingChannel.setCommunicationConfiguration(temp.getCommunicationConfiguration());
        }
        return existingChannel;
    }


    public void deleteCommunicationChannel(String clientId, String objectId) {
        validateClientId(clientId);
        AlertChannel channelToDelete = findExistingChannel(clientId, objectId);
        alertChannelRepository.deleteById(objectId);
    }
}
