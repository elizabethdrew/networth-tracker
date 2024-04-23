package com.drew.truelayerservice.exception;

import org.springframework.http.HttpStatus;

public class ConnectorOverloadException extends TrueLayerException {
    public ConnectorOverloadException(String detail) {
        super(detail, HttpStatus.SERVICE_UNAVAILABLE, "connector_overload");
    }
}
