package com.adyogi.notification.exceptions;

public class ServiceException extends RuntimeException {

    // Constructor to create a ServiceException with a message
    public ServiceException(String message) {
        super(message);
    }

    // Constructor to create a ServiceException with both a message and a cause
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    // Constructor to create a ServiceException with a cause
    public ServiceException(Throwable cause) {
        super(cause);
    }
}

