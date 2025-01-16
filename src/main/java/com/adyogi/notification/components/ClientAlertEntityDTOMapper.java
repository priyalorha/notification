package com.adyogi.notification.components;

import com.adyogi.notification.database.mongo.entities.ClientAlert;
import com.adyogi.notification.database.mongo.entities.TriggerCondition;
import com.adyogi.notification.dto.ClientAlertDTO;
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
import static com.adyogi.notification.utils.constants.ErrorConstants.ERROR_TRIGGER_CONDITION_DTO;

@Component
public class ClientAlertEntityDTOMapper {

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    TriggerConditionEntityDTOMapper triggerConditionEntityDTOMapper;

    @PostConstruct
    public void postConstruct() {
        // Configure mapping for ClientNotificationConfiguration and DTO without triggerConditions
        modelMapper.createTypeMap(ClientAlert.class, ClientAlertDTO.class)
                .addMappings(new PropertyMap<ClientAlert, ClientAlertDTO>() {
                    @Override
                    protected void configure() {
                        // Skip mapping for triggerConditions, will map manually
                        skip(destination.getTriggerConditions());
                    }
                });

        modelMapper.createTypeMap(ClientAlertDTO.class, ClientAlert.class)
                .addMappings(new PropertyMap<ClientAlertDTO, ClientAlert>() {
                    @Override
                    protected void configure() {
                        // Skip mapping for triggerConditions, will map manually
                        skip(destination.getTriggerConditions());
                    }
                });
    }

    // Convert entity to DTO with triggerConditions mapped manually
    public ClientAlertDTO convertEntityToDTO(ClientAlert clientAlert) {
        ClientAlertDTO clientAlertDTO = modelMapper.map(clientAlert, ClientAlertDTO.class);

        List<TriggerConditionDTO> triggerConditionDTOList = convertTriggerConditionsToDTO(clientAlert.getTriggerConditions());

        // Set the converted list of trigger conditions to the DTO
        clientAlertDTO.setTriggerConditions(triggerConditionDTOList);

        return clientAlertDTO;
    }

    // Convert DTO to entity with triggerConditions mapped manually
    public ClientAlert convertDTOToEntity(ClientAlertDTO clientAlertDTO) {
        ClientAlert clientAlert = modelMapper.map(clientAlertDTO, ClientAlert.class);

        List<TriggerCondition> triggerConditionList = convertTriggerConditionsToEntity(clientAlertDTO.getTriggerConditions());

        // Set the converted list of trigger conditions to the entity
        clientAlert.setTriggerConditions(triggerConditionList);

        return clientAlert;
    }

    // Helper method to convert list of TriggerCondition entities to DTOs
    @SneakyThrows
    private List<TriggerConditionDTO> convertTriggerConditionsToDTO(List<TriggerCondition> triggerConditions) {
        return triggerConditions.stream()
                .map(triggerConditionEntityDTOMapper::convertEntityToDTO)
                .collect(Collectors.toList());
    }

    // Helper method to convert list of TriggerCondition DTOs to entities
    private List<TriggerCondition> convertTriggerConditionsToEntity(List<TriggerConditionDTO> triggerConditionDTOs) {
        try {
            // Map each TriggerConditionDTO to a TriggerCondition entity
            return triggerConditionDTOs.stream()
                    .map(triggerConditionDTO -> {
                        try {
                            return triggerConditionEntityDTOMapper.convertDTOToEntity(triggerConditionDTO);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }) // Ensure correct mapping
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Generic exception handling for unexpected errors
            throw new RuntimeException(ERROR_TRIGGER_CONDITION_DTO, e);
        }
    }
}
