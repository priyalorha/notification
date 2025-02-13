package com.adyogi.notification.controller;

import com.adyogi.notification.dto.AlertDTO;
import com.adyogi.notification.services.AlertService;
import com.adyogi.notification.services.IncidentService;
import com.adyogi.notification.utils.constants.ConfigConstants;
import com.adyogi.notification.utils.logging.annotation.MDCValue;
import com.adyogi.notification.validators.OnCreate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.List;

import static com.adyogi.notification.utils.constants.ErrorConstants.INCIDENT_TRIGGERED_SUCCESSFULLY;
import static com.adyogi.notification.utils.constants.RequestDTOConstants.ALERT_ID;
import static com.adyogi.notification.utils.constants.RequestDTOConstants.CLIENT_ID;

@RestController
@RequestMapping("/clients")
@Valid
public class AlertController {

    @Autowired
    private AlertService alertService;

    @Autowired
    private IncidentService incidentService;


    @PostMapping("/{client_id}/alerts")
    public ResponseEntity<AlertDTO> createAlert(
            @MDCValue(ConfigConstants.CLIENT_ID_LOGGING_FIELD_NAME)
            @PathVariable(CLIENT_ID) @NotBlank String clientId,
            @Validated(OnCreate.class) @RequestBody AlertDTO configuration) throws IOException {

        return ResponseEntity.status(HttpStatus.CREATED).body(alertService.createAlert(
                        clientId,
                        configuration));
    }

    @GetMapping("/{client_id}/alerts")
    public ResponseEntity<List<AlertDTO>> getAlert(
            @MDCValue(ConfigConstants.CLIENT_ID_LOGGING_FIELD_NAME)
            @PathVariable(CLIENT_ID) @NotBlank String clientId) {
         ;
        return ResponseEntity.ok(alertService.getAlerts(clientId));
    }

    @GetMapping("/{client_id}/alerts/{alert_id}")
    public ResponseEntity<AlertDTO> getAlert(
            @MDCValue(ConfigConstants.CLIENT_ID_LOGGING_FIELD_NAME)
            @PathVariable(CLIENT_ID) @NotBlank String clientId,
            @PathVariable(ALERT_ID) @NotBlank String alertId) {

        return new ResponseEntity<>(alertService.getAlerts(
                        clientId, alertId),
                HttpStatus.OK);
    }

    @PutMapping("/{client_id}/alerts/{alert_id}")
    public ResponseEntity<AlertDTO> updateAlert(
            @MDCValue(ConfigConstants.CLIENT_ID_LOGGING_FIELD_NAME)
            @PathVariable(CLIENT_ID) @NotBlank String clientId,
            @PathVariable(ALERT_ID) @NotBlank String alertId,
            @Validated(OnCreate.class) @RequestBody AlertDTO configuration) {

        return new ResponseEntity<>(alertService.updateAlert(
                        clientId, alertId, configuration),
                HttpStatus.OK);
    }


    @PatchMapping("/{client_id}/alerts/{alert_id}")
    public ResponseEntity<AlertDTO> patchUpdate(
            @MDCValue(ConfigConstants.CLIENT_ID_LOGGING_FIELD_NAME)
            @PathVariable(CLIENT_ID) @NotBlank String clientId,
            @PathVariable(ALERT_ID) @NotBlank String alertId,
            @RequestBody AlertDTO configuration) {

        ;
        return new ResponseEntity<>(alertService.patchAlert(
                clientId, alertId, configuration),
                HttpStatus.OK);
    }

    @DeleteMapping("/{client_id}/alerts/{alert_id}")
    public ResponseEntity<Void> deleteAlert(
            @MDCValue(ConfigConstants.CLIENT_ID_LOGGING_FIELD_NAME)
            @PathVariable("client_id") @NotBlank String clientId,
            @PathVariable(ALERT_ID) @NotBlank String alertId) {

        alertService.deleteAlert(clientId, alertId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("{client_id}/alert:incident-trigger")
    public ResponseEntity<String> triggerIncidentForAllClients(
            @MDCValue(ConfigConstants.CLIENT_ID_LOGGING_FIELD_NAME)
            @PathVariable(CLIENT_ID) @NotBlank String clientId) throws IOException {
        incidentService.triggerIncidentForClient(clientId);
        return new ResponseEntity<>(INCIDENT_TRIGGERED_SUCCESSFULLY, HttpStatus.ACCEPTED);
    }

    @PostMapping("/alert:trigger-for-all-clients")
    public ResponseEntity<String> triggerIncidentForAllClients() {
        incidentService.triggerIncidentForAllClients();
        return new ResponseEntity<>(INCIDENT_TRIGGERED_SUCCESSFULLY, HttpStatus.ACCEPTED);
    }
}
