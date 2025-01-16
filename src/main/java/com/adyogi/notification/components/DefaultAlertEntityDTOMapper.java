package com.adyogi.notification.components;

import com.adyogi.notification.database.mongo.entities.DefaultAlert;
import com.adyogi.notification.database.mongo.entities.TriggerCondition;
import com.adyogi.notification.dto.DefaultAlertDTO;
import com.adyogi.notification.dto.TriggerConditionDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.SneakyThrows;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

import static com.adyogi.notification.utils.constants.ErrorConstants.ERROR_CONVERTING_DTO_CONDITION;
import static com.adyogi.notification.utils.constants.ErrorConstants.ERROR_TRIGGER_CONDITION_DTO;


@Component
public class DefaultAlertEntityDTOMapper {

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    TriggerConditionEntityDTOMapper triggerConditionEntityDTOMapper;


    @PostConstruct
    public void postConstruct() {
        // Configure mapping for ClientNotificationConfiguration and DTO without triggerConditions
        modelMapper.createTypeMap(DefaultAlert.class, DefaultAlertDTO.class)
                .addMappings(new PropertyMap<DefaultAlert, DefaultAlertDTO>() {
                    @Override
                    protected void configure() {
                        // Skip mapping for triggerConditions, will map manually
                        skip(destination.getTriggerConditions());
                    }
                });

        modelMapper.createTypeMap(DefaultAlertDTO.class, DefaultAlert.class)
                .addMappings(new PropertyMap<DefaultAlertDTO, DefaultAlert>() {
                    @Override
                    protected void configure() {
                        // Skip mapping for triggerConditions, will map manually
                        skip(destination.getTriggerConditions());
                    }
                });
    }

    @SneakyThrows
    public DefaultAlertDTO convertEntityToDTO(DefaultAlert defaultAlert) {
        DefaultAlertDTO defaultAlertDTO = modelMapper.map(defaultAlert, DefaultAlertDTO.class);

        try {
            List<TriggerConditionDTO> triggerConditionDTOList = defaultAlert.getTriggerConditions()
                    .stream()
                    .map(triggerConditionEntityDTOMapper::convertEntityToDTO)
                    .collect(Collectors.toList());

            // Set the converted list to the DTO
            defaultAlertDTO.setTriggerConditions(triggerConditionDTOList);

            return defaultAlertDTO;
        }
        catch (Exception e) {
            // Handle exception (log, etc.) and throw it
            throw new RuntimeException(ERROR_TRIGGER_CONDITION_DTO, e);
        }
    }




    public DefaultAlert convertDTOToEntity(DefaultAlertDTO defaultAlertDTO) {
        // Map the main DTO to the entity
        DefaultAlert defaultAlert = modelMapper.map(defaultAlertDTO, DefaultAlert.class);

        // Map the list of TriggerCondition DTOs to TriggerCondition entities
        List<TriggerCondition> triggerConditionList = defaultAlertDTO.getTriggerConditions()
                .stream()
                .map(triggerConditionDTO -> {
                    try {
                        return triggerConditionEntityDTOMapper.convertDTOToEntity(triggerConditionDTO); // Ensure this method handles DTO to entity conversion
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(ERROR_CONVERTING_DTO_CONDITION, e); // Handle the exception appropriately
                    }
                })
                .collect(Collectors.toList());

        // Set the converted list to the entity
        defaultAlert.setTriggerConditions(triggerConditionList);

        return defaultAlert;
    }


}

