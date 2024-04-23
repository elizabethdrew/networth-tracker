package com.drew.truelayerservice.exception;

import org.springframework.http.HttpStatus;

public class ProviderTimeoutException extends TrueLayerException {
    public ProviderTimeoutException(String detail) {
        super(detail, HttpStatus.GATEWAY_TIMEOUT, "provider_timeout");
    }
}
