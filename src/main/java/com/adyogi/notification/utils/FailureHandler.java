package com.adyogi.notification.utils;

import com.adyogi.notification.components.AlertRateLimiter;
import com.adyogi.notification.exceptions.ServiceException;
import com.adyogi.notification.utils.logging.LogUtil;
import com.adyogi.notification.utils.rollbar.RollbarManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.adyogi.notification.utils.constants.ErrorConstants.ERROR_ADDING_EMAIL_FOR_PROCESSING;
import static com.adyogi.notification.utils.constants.ErrorConstants.SENDGRID_FAILURE;

@Component
public class FailureHandler {

    Logger logger = LogUtil.getInstance();

    @Autowired
    private AlertRateLimiter failureCounter;

    public void handleFailure(String errorKey, Exception e) throws IOException, ServiceException {
        if (failureCounter.shouldAlert(errorKey)) {
            if (SENDGRID_FAILURE.equals(errorKey)
                    || ERROR_ADDING_EMAIL_FOR_PROCESSING.equals(errorKey)) {
                RollbarManager.sendExceptionOnCriticalRollBar(errorKey, e);
            } else {
                RollbarManager.sendExceptionOnRollBar(errorKey, e);
            }
        }
        logger.error(errorKey, e);
        if (e instanceof IOException) {
            throw (IOException) e;
        } else if (e instanceof ServiceException) {
            throw (ServiceException) e;
        } else {
            throw new RuntimeException(e);
        }
    }
}
