package com.drew.truelayerservice.exception;

import org.springframework.http.HttpStatus;

public class AccountNotFoundException extends TrueLayerException {
    public AccountNotFoundException(String detail) {
        super("The requested account cannot be found: " + detail, HttpStatus.NOT_FOUND, "account_not_found");
    }
}
