package com.drew.truelayerservice.exception;

import org.springframework.http.HttpStatus;

public abstract class TrueLayerException extends RuntimeException {
    private HttpStatus status;
    private String errorCode;

    public TrueLayerException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }
}