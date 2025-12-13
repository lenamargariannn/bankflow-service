package com.bankflow.exception;

import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException {

    private final String resourceType;
    private final Long resourceId;
    private final String resourceIdentifier;

    public NotFoundException(String message) {
        super(message);
        this.resourceType = null;
        this.resourceId = null;
        this.resourceIdentifier = null;
    }

    public NotFoundException(String resourceType, Long resourceId) {
        super(String.format("%s with ID %d not found", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.resourceIdentifier = String.valueOf(resourceId);
    }

    public NotFoundException(String resourceType, String identifier) {
        super(String.format("%s '%s' not found", resourceType, identifier));
        this.resourceType = resourceType;
        this.resourceId = null;
        this.resourceIdentifier = identifier;
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.resourceType = null;
        this.resourceId = null;
        this.resourceIdentifier = null;
    }

}
