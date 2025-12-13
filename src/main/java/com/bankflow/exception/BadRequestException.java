package com.bankflow.exception;

import lombok.Getter;

@Getter
public class BadRequestException extends RuntimeException {

    private final String fieldName;
    private final String fieldValue;

    public BadRequestException(String message) {
        super(message);
        this.fieldName = null;
        this.fieldValue = null;
    }

    public BadRequestException(String fieldName, String fieldValue, String reason) {
        super(String.format("Invalid value for field '%s': %s. Reason: %s", fieldName, fieldValue, reason));
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

}
