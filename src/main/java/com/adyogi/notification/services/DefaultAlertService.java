package com.adyogi.notification.services;

import com.adyogi.notification.components.DefaultAlertEntityDTOMapper;
import com.adyogi.notification.database.mongo.entities.DefaultAlert;
import com.adyogi.notification.dto.DefaultAlertDTO;
import com.adyogi.notification.exceptions.NotFoundException;
import com.adyogi.notification.repositories.back4app.DefaultAlertRepository;
import com.adyogi.notification.retrofits.IDefaultAlertRetrofit;
import com.adyogi.notification.retrofits.RetrofitParseInstanceService;
import com.adyogi.notification.utils.constants.ErrorConstants;
import com.adyogi.notification.utils.logging.LogUtil;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Component
public class DefaultAlertService {

    private final Logger logger = LogUtil.getInstance();
    private final DefaultAlertRepository repository;
    private final RetrofitParseInstanceService retrofitService;
    private final DefaultAlertEntityDTOMapper modelMapper;

    private final BaselineService baselineService;



    public DefaultAlertService(DefaultAlertRepository repository,
                               RetrofitParseInstanceService retrofitService,
                               DefaultAlertEntityDTOMapper modelMapper,
                               BaselineService baselineService) {

        this.repository = repository;
        this.retrofitService = retrofitService;
        this.modelMapper = modelMapper;
        this.baselineService = baselineService;
    }


    private DefaultAlert fetchDefaultAlert(String alertId) {
        Optional<DefaultAlert> alert =  repository.findById(alertId);
        if (alert.isEmpty()) {
            throw new NotFoundException(String.format(ErrorConstants.ALERT_NOT_FOUND, alertId));
        }
        return alert.get();
    }


    public DefaultAlertDTO getDefaultAlert(String alertId) {

        return modelMapper.convertEntityToDTO(fetchDefaultAlert(alertId));
    }

    public List<DefaultAlertDTO> getDefaultAlert() {

        return repository.findAll()
                .stream()
                .map(modelMapper::convertEntityToDTO)
                .collect(Collectors.toList());
    }

    public DefaultAlertDTO createDefaultAlert(DefaultAlertDTO configuration) throws IOException, IOException {

//        return modelMapper.convertEntityToDTO(repository.save(modelMapper.convertDTOToEntity(configuration)));
        DefaultAlert configEntity = modelMapper.convertDTOToEntity(configuration);
        Retrofit retrofitInstance = retrofitService.getRetrofitInstanceWithJacksonConverter();
        IDefaultAlertRetrofit retrofitApi = retrofitInstance.create(IDefaultAlertRetrofit.class);

        Response<DefaultAlert> response = retrofitApi.saveConfiguration(configEntity).execute();

        if (response.isSuccessful()) {
            configuration.setObjectId(String.valueOf(response.body().getObjectId()));
            configuration.setCreatedAt(response.body().getCreatedAt());
            configuration.setUpdatedAt(response.body().getUpdatedAt());
            // adding this to create new baseline for alerts, in case this metric_name already exists
            baselineService.generateBaselineForAlerts(response.body().getObjectId());
            return configuration;
        }

        throw new RuntimeException(String.valueOf(response.errorBody()));
    }

    public DefaultAlertDTO updateDefaultAlert(String alertId,
                                              DefaultAlertDTO configuration) {


        DefaultAlert existingConfig = fetchDefaultAlert(alertId);
        DefaultAlert updatedConfig = modelMapper.convertDTOToEntity(configuration);
        updatedConfig.setObjectId(existingConfig.getObjectId());
        updatedConfig.setUpdatedAt(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        updatedConfig.setCreatedAt(existingConfig.getCreatedAt());

        return modelMapper.convertEntityToDTO(repository.save(updatedConfig));
    }

    public void deleteDefaultAlert(String alertId) {

        DefaultAlert config = fetchDefaultAlert(alertId);
        repository.delete(config);
    }

}

