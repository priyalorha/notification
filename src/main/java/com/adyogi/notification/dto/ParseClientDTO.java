package com.adyogi.notification.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import static com.adyogi.notification.utils.constants.MongoConstants.PARSE_CLIENT_OBJECT_ID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor // Include NoArgsConstructor for deserialization (optional)
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class ParseClientDTO {


    @JsonProperty(PARSE_CLIENT_OBJECT_ID)
    private String clientId;
}
