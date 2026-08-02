package com.builder.url_shortener.exception;

import org.springframework.http.HttpStatus;

public class ResourceExpiredException extends ApiException {

    public ResourceExpiredException(String messageKey, Object... args) {
        super(HttpStatus.GONE, messageKey, args);
    }
}
