package com.adyogi.notification.exceptions;

public class ClientValidationException extends RuntimeException {
    private final String message;

    public ClientValidationException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
