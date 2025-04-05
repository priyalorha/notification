package com.adyogi.notification.components;

import com.adyogi.notification.dto.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.adyogi.notification.utils.constants.MongoConstants.*;

@Component
public class ValueEntityDTOMapper {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ModelMapper modelMapper;

    // Convert JSON string (Entity) to DTO
    public <T> T convertEntityToDTO(String valueEntity, Class<T> dtoClass) throws JsonProcessingException {
        if (valueEntity != null) {
            return objectMapper.readValue(valueEntity, dtoClass);
        }
        return null;
    }

    // Convert JSON string (DTO) to Entity
    public <T> T convertDTOToEntity(String valueDTO, Class<T> entityClass) throws JsonProcessingException {
        if (valueDTO != null) {
            return objectMapper.readValue(valueDTO, entityClass);
        }
        return null;
    }

    // Convert a ValueDTO object to the corresponding Entity object based on its type
    public String convertDTOToEntity(ValueDTO valueDTO) throws JsonProcessingException {
        if (valueDTO != null) {
            return objectMapper.writeValueAsString(valueDTO);
        }
        return null;
    }

    // Utility method to map a specific string to a DTO class based on its content
    public Object mapEntityToDTO(String valueEntity) throws JsonProcessingException {

        return objectMapper.readValue(valueEntity, ValueDTO.class);
    }
}
