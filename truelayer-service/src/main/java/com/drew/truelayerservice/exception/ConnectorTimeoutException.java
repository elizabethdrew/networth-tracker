package com.drew.truelayerservice.exception;

import org.springframework.http.HttpStatus;

public class ConnectorTimeoutException extends TrueLayerException {
    public ConnectorTimeoutException(String detail) {
        super(detail, HttpStatus.GATEWAY_TIMEOUT, "connector_timeout");
    }
}
