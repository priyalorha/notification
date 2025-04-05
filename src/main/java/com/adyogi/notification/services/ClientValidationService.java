package com.adyogi.notification.services;

import com.adyogi.notification.exceptions.NotFoundException;
import com.adyogi.notification.repositories.back4app.ClientRepository;
import com.adyogi.notification.utils.constants.ErrorConstants;
import com.adyogi.notification.utils.logging.LogUtil;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import static com.adyogi.notification.utils.constants.ConfigConstants.*;

@Service
public class ClientValidationService {

    private final Logger logger = LogUtil.getInstance();

    @Autowired
    private ClientRepository clientRepository; // assuming you have a repository to fetch client data

    @Cacheable(value = CLIENT_CACHE, key = CLIENT_ID_KEY)
    public boolean isClientIdValid(String clientId) {
        return clientRepository.existingActiveClient(clientId);
    }

    @Cacheable(value = VALIDATE_CLIENT_ID, key = CLIENT_ID_KEY)
    public void validateClientId(String clientId) {
        if (!isClientIdValid(clientId)) {
            String errorMessage = String.format(ErrorConstants.INVALID_CLIENT_ID, clientId);
            logger.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
    }
}
