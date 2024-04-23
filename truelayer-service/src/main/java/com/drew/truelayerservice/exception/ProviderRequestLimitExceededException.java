package com.drew.truelayerservice.exception;

import org.springframework.http.HttpStatus;

public class ProviderRequestLimitExceededException extends TrueLayerException {
    public ProviderRequestLimitExceededException(String detail) {
        super(detail, HttpStatus.TOO_MANY_REQUESTS, "provider_request_limit_exceeded");
    }
}
