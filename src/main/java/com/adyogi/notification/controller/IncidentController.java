package com.adyogi.notification.controller;

import com.adyogi.notification.database.sql.entities.Incident;
import com.adyogi.notification.dto.IncidentDTO;
import com.adyogi.notification.services.IncidentService;
import com.adyogi.notification.utils.constants.ConfigConstants;
import com.adyogi.notification.utils.logging.annotation.MDCValue;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.List;

import static com.adyogi.notification.utils.constants.ConfigConstants.*;
import static com.adyogi.notification.utils.constants.ErrorConstants.*;
import static com.adyogi.notification.utils.constants.RequestDTOConstants.CLIENT_ID;
import static com.adyogi.notification.utils.constants.RequestDTOConstants.INCIDENT_ID;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    @GetMapping("{client_id}/incident")
    public ResponseEntity<List<IncidentDTO>> getIncidentsByClientId(
            @MDCValue(ConfigConstants.CLIENT_ID_LOGGING_FIELD_NAME)
            @PathVariable(CLIENT_ID) @NotBlank String clientId,
            @RequestParam(name = PAGE, required = false, defaultValue = PAGE_SIZE) Integer page,
            @RequestParam(name = LIMIT, required = false, defaultValue = PAGE_LIMIT) Integer limit) {
        return ResponseEntity.ok(incidentService.getIncidentsByClientId(clientId,
                page,
                limit));
    }

    @GetMapping("{client_id}/incident/{incident_id}")
    public ResponseEntity<IncidentDTO> getIncidentById(
            @MDCValue(ConfigConstants.CLIENT_ID_LOGGING_FIELD_NAME)
            @PathVariable(CLIENT_ID) @NotBlank String clientId,
            @PathVariable(INCIDENT_ID) @NotBlank String incidentId) {
        return ResponseEntity.ok(incidentService.getIncidentById(clientId, incidentId));
    }

    @PostMapping("{client_id}/incident/{incident_id}:resolve")
    public ResponseEntity<String> resolveIncident(
            @MDCValue(ConfigConstants.CLIENT_ID_LOGGING_FIELD_NAME)
            @PathVariable(CLIENT_ID) @NotBlank String clientId,
            @PathVariable(INCIDENT_ID) @NotBlank String incidentId) {
        incidentService.resolveIncident(clientId, incidentId);
        return ResponseEntity.ok(INCIDENT_RESOLVED_SUCCESSFULLY);
    }

    @PostMapping("{client_id}/incident/{incident_id}:pause")
    public ResponseEntity<String> pauseIncident(
            @MDCValue(ConfigConstants.CLIENT_ID_LOGGING_FIELD_NAME)
            @PathVariable(CLIENT_ID) @NotBlank String clientId,
            @PathVariable(INCIDENT_ID) @NotBlank String incidentId) {
        incidentService.pauseIncidentAlerts(clientId, incidentId);
        return ResponseEntity.ok(ALERTS_FOR_INCIDENT_PAUSED_SUCCESSFULLY);
    }

    @PostMapping("{client_id}/incident/{incident_id}:enable")
    public ResponseEntity<String> enableIncidentAlerts(
            @MDCValue(ConfigConstants.CLIENT_ID_LOGGING_FIELD_NAME)
            @PathVariable(CLIENT_ID) @NotBlank String clientId,
            @PathVariable(INCIDENT_ID) @NotBlank String incidentId) {
        incidentService.enableIncidentAlert(clientId, incidentId);
        return ResponseEntity.ok(ALERTS_FOR_INCIDENT_ENABLED_SUCCESSFULLY);
    }


    @PostMapping("/incident:trigger")
    public ResponseEntity<String> triggerIncidentForAllClients(
            @MDCValue(ConfigConstants.CLIENT_ID_LOGGING_FIELD_NAME)
            @PathVariable(CLIENT_ID) @NotBlank String clientId) {
        incidentService.triggerIncidentForClient(clientId);
        return ResponseEntity.ok(INCIDENT_TRIGGERED_SUCCESSFULLY);
    }

    @PostMapping("/incident:trigger-for-all-clients")
    public ResponseEntity<String> triggerIncidentForAllClients() {
        incidentService.triggerIncidentForAllClients();
        return ResponseEntity.ok(INCIDENT_TRIGGERED_SUCCESSFULLY);
    }

    @PostMapping("{client_id}/incident:email")
    public  ResponseEntity<String> triggerEmailForClient(
            @MDCValue(ConfigConstants.CLIENT_ID_LOGGING_FIELD_NAME)
            @PathVariable(CLIENT_ID) @NotBlank String clientId) {
    incidentService.triggerEmailForClient(clientId);
        return ResponseEntity.ok(INCIDENT_TRIGGERED_SUCCESSFULLY);
    }

    @PostMapping("/incident:email")
    public  ResponseEntity<String> triggerEmailForAllClient() {
        incidentService.triggerEmailForAllClient();
        return ResponseEntity.ok(INCIDENT_TRIGGERED_SUCCESSFULLY);
    }
}
