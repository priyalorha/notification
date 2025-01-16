package com.adyogi.notification.controller;

import com.adyogi.notification.dto.DefaultAlertDTO;
import com.adyogi.notification.services.DefaultAlertService;
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

@RestController
@RequestMapping("/admin")
@Valid
public class DefaultAlertController {

    @Autowired
    private final DefaultAlertService defaultNotificationService;

    public DefaultAlertController(DefaultAlertService defaultNotificationService) {
        this.defaultNotificationService = defaultNotificationService;
    }

    @PostMapping("/alert")
    public ResponseEntity<DefaultAlertDTO> createDefaultNotificationConfiguration(
            @MDCValue(ConfigConstants.DEFAULT_LOGGING_FIELD_NAME)
            @Valid @RequestBody DefaultAlertDTO configuration) throws IOException {

        DefaultAlertDTO createdConfig =
                defaultNotificationService.createDefaultAlert(configuration);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdConfig);
    }

    @GetMapping("/alert")
    public ResponseEntity<List<DefaultAlertDTO>> getDefaultAlert() {

        List<DefaultAlertDTO> configurations =
                defaultNotificationService.getDefaultAlert();
        return ResponseEntity.ok(configurations);
    }

    @GetMapping("/alert/{alert_id}")
    public ResponseEntity<DefaultAlertDTO> getDefaultAlert(
            @MDCValue(ConfigConstants.DEFAULT_LOGGING_FIELD_NAME)
            @PathVariable(ALERT_ID) @NotBlank String alertId) {

        return new ResponseEntity<>(
                defaultNotificationService.getDefaultAlert(alertId),
                HttpStatus.OK);
    }

    @PutMapping("/alert/{alert_id}")
    public ResponseEntity<DefaultAlertDTO> updateDefaultAlert(
            @MDCValue(ConfigConstants.DEFAULT_LOGGING_FIELD_NAME)
            @PathVariable(ALERT_ID) @NotBlank String alertId,
            @Valid @RequestBody DefaultAlertDTO configuration) {

        ;
        return new ResponseEntity<>(
                defaultNotificationService.updateDefaultAlert(
                         alertId, configuration),
                HttpStatus.OK);
    }

    @DeleteMapping("/alert/{alert_id}")
    public ResponseEntity<Void> deleteDefaultAlert(
            @MDCValue(ConfigConstants.DEFAULT_LOGGING_FIELD_NAME)
            @PathVariable(ALERT_ID) @NotBlank String alertId) {

        defaultNotificationService.deleteDefaultAlert( alertId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
