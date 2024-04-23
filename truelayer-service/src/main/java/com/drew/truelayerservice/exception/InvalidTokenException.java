package com.drew.truelayerservice.exception;

import org.springframework.http.HttpStatus;

public class InvalidTokenException extends TrueLayerException {
    public InvalidTokenException(String detail) {
        super("The token is no longer valid: " + detail, HttpStatus.UNAUTHORIZED, "invalid_token");
    }
}
