package com.drew.truelayerservice.exception;

import org.springframework.http.HttpStatus;

public class ProviderErrorException extends TrueLayerException {
    public ProviderErrorException(String detail) {
        super(detail, HttpStatus.SERVICE_UNAVAILABLE, "provider_error");
    }
}
