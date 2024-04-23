package com.drew.truelayerservice.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends TrueLayerException {
    public UnauthorizedException(String detail) {
        super("The credentials or token are no longer valid: " + detail, HttpStatus.UNAUTHORIZED, "unauthorized");
    }
}
