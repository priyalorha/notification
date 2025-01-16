package com.adyogi.notification.dto.emails;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailAlertSummaryDTO {
    private List<StoplossAlertDTO> stoplossAlerts;
    private List<ProductsetAlertDTO> productSets;
    private List<IntegrationAlertDTO> integrationAlerts;
    private String email;
    private String teamName;
}
