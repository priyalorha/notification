package com.adyogi.notification.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {

    @GetMapping(value="status")
    public ResponseEntity<String> checkStatus(){
        return new ResponseEntity<String>(HttpStatus.OK);
    }
}
