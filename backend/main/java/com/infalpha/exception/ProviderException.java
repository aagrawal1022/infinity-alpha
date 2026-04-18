package com.infalpha.exception;

/**
 * Exception thrown when an LLM provider encounters an error.
 */
public class ProviderException extends RuntimeException {

    private final String providerKey;

    public ProviderException(String providerKey, String message) {
        super(message);
        this.providerKey = providerKey;
    }

    public ProviderException(String providerKey, String message, Throwable cause) {
        super(message, cause);
        this.providerKey = providerKey;
    }

    public String getProviderKey() {
        return providerKey;
    }
}
