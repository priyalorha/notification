package com.adyogi.notification.services;

import com.adyogi.notification.components.ClientAlertEntityDTOMapper;
import com.adyogi.notification.database.mongo.entities.ClientAlert;
import com.adyogi.notification.dto.ClientAlertDTO;
import com.adyogi.notification.exceptions.NotFoundException;
import com.adyogi.notification.repositories.back4app.ClientAlertRepository;
import com.adyogi.notification.retrofits.IClientAlertRetrofit;
import com.adyogi.notification.retrofits.RetrofitParseInstanceService;
import com.adyogi.notification.utils.constants.ErrorConstants;
import com.adyogi.notification.utils.logging.LogUtil;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class ClientAlertService {

    private final Logger logger = LogUtil.getInstance();
    private final ClientValidationService clientValidationService;
    private final ClientAlertRepository repository;
    private final RetrofitParseInstanceService retrofitService;
    private final ClientAlertEntityDTOMapper modelMapper;
    private final BaselineService baselineService;

    public ClientAlertService(ClientValidationService clientValidationService,
                              ClientAlertRepository repository,
                              RetrofitParseInstanceService retrofitService,
                              ClientAlertEntityDTOMapper modelMapper,
                              BaselineService baselineService) {
        this.clientValidationService = clientValidationService;
        this.repository = repository;
        this.retrofitService = retrofitService;
        this.modelMapper = modelMapper;
        this.baselineService = baselineService;
    }


    public void validateClientId(String clientId) {
        if (!clientValidationService.isClientIdValid(clientId)) {
            String errorMessage = String.format(ErrorConstants.INVALID_CLIENT_ID, clientId);
            logger.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
    }

    private ClientAlert fetchClientNotificationConfiguration(String clientId, String alertId) {
        // Fetch the configuration from the repository
        ClientAlert config = repository.findByObjectIdAndClientId(alertId, clientId);

        if (config == null) {
            String errorMessage = String.format(ErrorConstants.ALERT_NOT_FOUND_MESSAGE, alertId, clientId);
            logger.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        return config;
    }

    public ClientAlertDTO getClientAlert(String clientId, String alertId) {
        validateClientId(clientId);
        return modelMapper.convertEntityToDTO(fetchClientNotificationConfiguration(clientId, alertId));
    }

    public List<ClientAlertDTO> getClientAlert(String clientId) {
        validateClientId(clientId);
        return repository.findByClientId(clientId)
                .stream()
                .map(modelMapper::convertEntityToDTO)
                .collect(Collectors.toList());
    }

    public ClientAlertDTO createClientAlert(String clientId,
                                            ClientAlertDTO configuration) throws IOException, IOException {
        validateClientId(clientId);
        configuration.setClientId(clientId);

        ClientAlert configEntity = modelMapper.convertDTOToEntity(configuration);
        Retrofit retrofitInstance = retrofitService.getRetrofitInstanceWithJacksonConverter();
        IClientAlertRetrofit retrofitApi = retrofitInstance.create(IClientAlertRetrofit.class);

        Response<ClientAlert> response = retrofitApi.saveConfiguration(configEntity).execute();
        if (response.isSuccessful()) {
            configuration.setObjectId(String.valueOf(response.body().getObjectId()));
            configuration.setCreatedAt(response.body().getCreatedAt());
            // adding this to create new baseline for alerts, in case this metric_name already exists
            baselineService.generateBaselineForAlerts(response.body().getObjectId(), clientId);
            return configuration;
        }

        throw new RuntimeException(String.valueOf(response.errorBody()));
    }

    public ClientAlertDTO updateClientAlert(String clientId,
                                            String alertId,
                                            ClientAlertDTO configuration) {
        validateClientId(clientId);

        ClientAlert existingConfig = fetchClientNotificationConfiguration(clientId, alertId);
        ClientAlert updatedConfig = modelMapper.convertDTOToEntity(configuration);
        updatedConfig.setObjectId(existingConfig.getObjectId());
        updatedConfig.setClientId(existingConfig.getClientId());

        return modelMapper.convertEntityToDTO(repository.save(updatedConfig));
    }

    public void deleteClientAlert(String clientId, String alertId) {
        validateClientId(clientId);
        ClientAlert config = fetchClientNotificationConfiguration(clientId, alertId);
        repository.delete(config);
    }
}
