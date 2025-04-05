package com.adyogi.notification.controller;


import com.adyogi.notification.dto.AlertChannelDTO;
import com.adyogi.notification.services.AlertChannelService;
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

import static com.adyogi.notification.utils.constants.RequestDTOConstants.CLIENT_ID;
import static com.adyogi.notification.utils.constants.RequestDTOConstants.OBJECT_ID;

@RestController
@RequestMapping("/clients")
@Valid
public class AlertChannelController {

    @Autowired
    private AlertChannelService alertChannelService;

    @PostMapping("/{client_id}/alert-channels")
    public ResponseEntity<AlertChannelDTO> createOrUpdateMetrics(
            @MDCValue(ConfigConstants.CLIENT_ID_LOGGING_FIELD_NAME)
            @PathVariable(CLIENT_ID) @NotBlank String clientId,
            @Validated(OnCreate.class) @RequestBody AlertChannelDTO alertChannelDTO) throws IOException {

        return new ResponseEntity<>(
                alertChannelService
                        .storeAlertChannel(
                        clientId,
                        alertChannelDTO),
                HttpStatus.CREATED);
    }

    @GetMapping("/{client_id}/alert-channels")
    public ResponseEntity<List<AlertChannelDTO>> getAllChannelsByClientId(
            @MDCValue(ConfigConstants.CLIENT_ID_LOGGING_FIELD_NAME)
            @PathVariable(CLIENT_ID) @NotBlank String clientId) {

        return new ResponseEntity<>(
                alertChannelService
                        .getAllAlertChannels(clientId),
                HttpStatus.OK);
    }


    // Update a specific communication channel by clientId and objectId
    @PutMapping("/{client_id}/alert-channels/{object_id}")
    public ResponseEntity<AlertChannelDTO> updateAlertChannel(
            @MDCValue(ConfigConstants.CLIENT_ID_LOGGING_FIELD_NAME)
            @PathVariable(CLIENT_ID) @NotBlank String clientId,
            @PathVariable(OBJECT_ID) @NotBlank String objectId,
            @Validated(OnCreate.class) @RequestBody AlertChannelDTO alertChannelDTO) throws IOException {

        return new ResponseEntity<>(alertChannelService
                .updateAlertChannel(
                clientId, objectId, alertChannelDTO), HttpStatus.OK);
    }

    @GetMapping("/{client_id}/alert-channels/{object_id}")
    public ResponseEntity<AlertChannelDTO> getAlertChannel(
            @MDCValue(ConfigConstants.CLIENT_ID_LOGGING_FIELD_NAME)
            @PathVariable(CLIENT_ID) @NotBlank String clientId,
            @PathVariable(OBJECT_ID) @NotBlank String objectId) {

        return new ResponseEntity<>(alertChannelService
                .getAlertChannel(
                        clientId, objectId), HttpStatus.OK);
    }

    // Delete a specific communication channel by clientId and objectId
    @DeleteMapping("/{client_id}/alert-channels/{object_id}")
    public ResponseEntity<Void> deleteChannel(
            @MDCValue(ConfigConstants.CLIENT_ID_LOGGING_FIELD_NAME)
            @PathVariable(CLIENT_ID) @NotBlank String clientId,
            @PathVariable(OBJECT_ID) @NotBlank String objectId) {

        alertChannelService
                .deleteAlertChannel(clientId, objectId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @PatchMapping("/{client_id}/alert-channels/{object_id}")
    public ResponseEntity<AlertChannelDTO> patchAlertChannel(
            @MDCValue(ConfigConstants.CLIENT_ID_LOGGING_FIELD_NAME)
            @PathVariable(CLIENT_ID) @NotBlank String clientId,
            @PathVariable(OBJECT_ID) @NotBlank String objectId,
            @RequestBody AlertChannelDTO alertChannelDTO)throws IOException  {
        return new ResponseEntity<>(alertChannelService
                .patchAlertChannel(clientId, objectId, alertChannelDTO),
                HttpStatus.OK);
    }
}
