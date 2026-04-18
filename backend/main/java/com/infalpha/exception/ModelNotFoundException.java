package com.infalpha.exception;

/**
 * Exception thrown when a requested model or provider is not found in the registry.
 */
public class ModelNotFoundException extends RuntimeException {

    public ModelNotFoundException(String message) {
        super(message);
    }
}
