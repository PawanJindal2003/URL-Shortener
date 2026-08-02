package com.builder.url_shortener.exception;

import org.springframework.http.HttpStatus;

public class InternalServerException extends ApiException {

    public InternalServerException(String messageKey, Object... args) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, messageKey, args);
    }
}
