package com.drew.truelayerservice.exception;

import org.springframework.http.HttpStatus;

public class ProviderRateLimitExceededException extends TrueLayerException {
    public ProviderRateLimitExceededException(String detail) {
        super(detail, HttpStatus.TOO_MANY_REQUESTS, "provider_too_many_requests");
    }
}
