package com.adyogi.notification.components;

import com.adyogi.notification.database.mongo.entities.TriggerCondition;
import com.adyogi.notification.dto.TriggerConditionDTO;
import com.adyogi.notification.dto.ValueDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static com.adyogi.notification.utils.constants.ErrorConstants.CONVERSION_FAILED;

@Component
public class TriggerConditionEntityDTOMapper {

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    ValueEntityDTOMapper valueEntityDTOMapper;

    @PostConstruct
    public void postConstruct() {
        modelMapper.createTypeMap(TriggerCondition.class, TriggerConditionDTO.class)
                .addMappings(mapper -> mapper.skip(TriggerConditionDTO::setValue));  // Skip value in DTO

        modelMapper.createTypeMap(TriggerConditionDTO.class, TriggerCondition.class)
                .addMappings(mapper -> mapper.skip(TriggerCondition::setValue));
    }

    public TriggerConditionDTO convertEntityToDTO(TriggerCondition triggerConditionEntity) {
        try {

            TriggerConditionDTO triggerConditionDTO = modelMapper.map(triggerConditionEntity,
                    TriggerConditionDTO.class);

            triggerConditionDTO.setValue(valueEntityDTOMapper.convertDTOToEntity(triggerConditionEntity.getValue(), ValueDTO.class));
            return triggerConditionDTO;
        } catch (Exception e) {
            // Handle exception (log, etc.) and throw it
            throw new RuntimeException(CONVERSION_FAILED, e);
        }
    }


    public TriggerCondition convertDTOToEntity(TriggerConditionDTO triggerConditionDTO) throws JsonProcessingException {
        TriggerCondition triggerCondition =  modelMapper.map(triggerConditionDTO,
                TriggerCondition.class);


        triggerCondition.setValue(valueEntityDTOMapper.convertDTOToEntity(triggerConditionDTO.getValue()));
        return triggerCondition;

    }

}