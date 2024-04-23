package com.drew.truelayerservice.exception;

import org.springframework.http.HttpStatus;

public class ScaExceededException extends TrueLayerException {
    public ScaExceededException(String detail) {
        super(detail, HttpStatus.FORBIDDEN, "sca_exceeded");
    }
}
