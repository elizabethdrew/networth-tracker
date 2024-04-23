package com.drew.truelayerservice.exception;

import org.springframework.http.HttpStatus;

public class AccessDeniedException extends TrueLayerException {
    public AccessDeniedException(String detail) {
        super(detail, HttpStatus.FORBIDDEN, "access_denied");
    }
}
