package com.adyogi.notification.services;

import com.adyogi.notification.repositories.back4app.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientValidationService {

    @Autowired
    private ClientRepository clientRepository; // assuming you have a repository to fetch client data

    public boolean isClientIdValid(String clientId) {
        // Implement logic to check if clientId is valid, for example, checking if it exists in the database
        return clientRepository.existsById(clientId);
    }
}
