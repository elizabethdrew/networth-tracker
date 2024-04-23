package com.drew.truelayerservice.exception;

import org.springframework.http.HttpStatus;

public class InternalServerErrorException extends TrueLayerException {
    public InternalServerErrorException(String detail) {
        super("Internal server error: " + detail, HttpStatus.INTERNAL_SERVER_ERROR, "internal_server_error");
    }
}
