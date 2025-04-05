package com.adyogi.notification.controller;

import com.adyogi.notification.dto.IncidentDTO;
import com.adyogi.notification.services.IncidentService;
import com.adyogi.notification.utils.constants.ConfigConstants;
import com.adyogi.notification.utils.logging.annotation.MDCValue;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @GetMapping("{client_id}/incidents")
    public ResponseEntity<List<IncidentDTO>> getIncidentsByClientId(
            @MDCValue(ConfigConstants.CLIENT_ID_LOGGING_FIELD_NAME)
            @PathVariable(CLIENT_ID) @NotBlank String clientId,
            @RequestParam(name = PAGE, required = false, defaultValue = PAGE_SIZE) Integer page,
            @RequestParam(name = LIMIT, required = false, defaultValue = PAGE_LIMIT) Integer limit) {
        return ResponseEntity.ok(incidentService.getIncidentsByClientId(clientId,
                page,
                limit));
    }

    @GetMapping("{client_id}/incidents/{incident_id}")
    public ResponseEntity<IncidentDTO> getIncidentById(
            @MDCValue(ConfigConstants.CLIENT_ID_LOGGING_FIELD_NAME)
            @PathVariable(CLIENT_ID) @NotBlank String clientId,
            @PathVariable(INCIDENT_ID) @NotBlank String incidentId) {
        return ResponseEntity.ok(incidentService.getIncidentById(clientId, incidentId));
    }

    @PatchMapping("{client_id}/incidents/{incident_id}")
    public ResponseEntity<String> patchIncident(
            @MDCValue(ConfigConstants.CLIENT_ID_LOGGING_FIELD_NAME)
            @PathVariable(CLIENT_ID) @NotBlank String clientId,
            @PathVariable(INCIDENT_ID) @NotBlank String incidentId,
            @RequestBody IncidentDTO incidentDTO) {
        incidentService.patchIncident(clientId, incidentId , incidentDTO);
        return ResponseEntity.ok(INCIDENT_UPDATED_SUCCESSFULLY);
    }

    @PostMapping("{client_id}:notify-incidents")
    public  ResponseEntity<String> triggerEmailForClient(
            @MDCValue(ConfigConstants.CLIENT_ID_LOGGING_FIELD_NAME)
            @PathVariable(CLIENT_ID) @NotBlank String clientId) {
        incidentService.triggerEmailForClient(clientId);
        return new ResponseEntity<>(EMAIL_TRIGGERED_SUCCESSFULLY, HttpStatus.ACCEPTED);
    }

    @PostMapping("/notify-incidents")
    public  ResponseEntity<String> triggerEmailForAllClient() {
        incidentService.triggerEmailForAllClient();
        return new ResponseEntity<>(EMAIL_TRIGGERED_SUCCESSFULLY, HttpStatus.ACCEPTED);
    }
}
