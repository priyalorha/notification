package com.adyogi.notification.controller;

import com.adyogi.notification.dto.ClientAlertDTO;
import com.adyogi.notification.services.ClientAlertService;
import com.adyogi.notification.utils.constants.ConfigConstants;
import com.adyogi.notification.utils.logging.annotation.MDCValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.List;

import static com.adyogi.notification.utils.constants.RequestDTOConstants.ALERT_ID;
import static com.adyogi.notification.utils.constants.RequestDTOConstants.CLIENT_ID;

@RestController
@RequestMapping("/clients")
@Valid
public class ClientAlertController {

    @Autowired
    private final ClientAlertService clientAlertService;

    public ClientAlertController(ClientAlertService clientAlertService) {
        this.clientAlertService = clientAlertService;
    }

    @PostMapping("/{client_id}/alert")
    public ResponseEntity<ClientAlertDTO> createClientAlert(
            @MDCValue(ConfigConstants.CLIENT_ID_LOGGING_FIELD_NAME)
            @PathVariable(CLIENT_ID) @NotBlank String clientId,
            @Valid @RequestBody ClientAlertDTO configuration) throws IOException {

        return ResponseEntity.status(HttpStatus.CREATED).body(clientAlertService.createClientAlert(
                        clientId,
                        configuration));
    }

    @GetMapping("/{client_id}/alert")
    public ResponseEntity<List<ClientAlertDTO>> getClientAlert(
            @MDCValue(ConfigConstants.CLIENT_ID_LOGGING_FIELD_NAME)
            @PathVariable(CLIENT_ID) @NotBlank String clientId) {
         ;
        return ResponseEntity.ok(clientAlertService.getClientAlert(clientId));
    }

    @GetMapping("/{client_id}/alert/{alert_id}")
    public ResponseEntity<ClientAlertDTO> getClientAlert(
            @MDCValue(ConfigConstants.CLIENT_ID_LOGGING_FIELD_NAME)
            @PathVariable(CLIENT_ID) @NotBlank String clientId,
            @PathVariable(ALERT_ID) @NotBlank String alertId) {

        return new ResponseEntity<>(clientAlertService.getClientAlert(
                        clientId, alertId),
                HttpStatus.OK);
    }

    @PutMapping("/{client_id}/alert/{alert_id}")
    public ResponseEntity<ClientAlertDTO> updateClientAlert(
            @MDCValue(ConfigConstants.CLIENT_ID_LOGGING_FIELD_NAME)
            @PathVariable(CLIENT_ID) @NotBlank String clientId,
            @PathVariable(ALERT_ID) @NotBlank String alertId,
            @Valid @RequestBody ClientAlertDTO configuration) {

        ;
        return new ResponseEntity<>(clientAlertService.updateClientAlert(
                        clientId, alertId, configuration),
                HttpStatus.OK);
    }

    @DeleteMapping("/{client_id}/alert/{alertId}")
    public ResponseEntity<Void> deleteClientAlert(
            @MDCValue(ConfigConstants.CLIENT_ID_LOGGING_FIELD_NAME)
            @PathVariable("client_id") @NotBlank String clientId,
            @PathVariable(ALERT_ID) @NotBlank String alertId) {

        clientAlertService.deleteClientAlert(clientId, alertId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
