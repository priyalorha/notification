package com.adyogi.notification.dto.emails;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IntegrationAlertDTO {
    private String title;
    private String description;
    private String impact;
    private String action;
    private String ctaUrl;
    private String ctaText;
}
