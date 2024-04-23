package com.drew.truelayerservice.exception;

import org.springframework.http.HttpStatus;

public class EndpointNotSupportedException extends TrueLayerException {
    public EndpointNotSupportedException(String detail) {
        super(detail, HttpStatus.NOT_IMPLEMENTED, "endpoint_not_supported");
    }
}
