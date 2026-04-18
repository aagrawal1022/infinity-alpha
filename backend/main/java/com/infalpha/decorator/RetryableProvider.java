package com.infalpha.decorator;

import com.infalpha.exception.ProviderException;
import com.infalpha.model.*;
import com.infalpha.provider.LlmProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

/**
 * Decorator Pattern — Adds retry with exponential backoff to any LlmProvider.
 */
public class RetryableProvider implements LlmProvider {

    private static final Logger log = LoggerFactory.getLogger(RetryableProvider.class);

    private final LlmProvider delegate;
    private final int maxAttempts;
    private final long backoffMs;

    public RetryableProvider(LlmProvider delegate, int maxAttempts, long backoffMs) {
        this.delegate = delegate;
        this.maxAttempts = maxAttempts;
        this.backoffMs = backoffMs;
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        Exception lastException = null;
        String key = getDescriptor().getKey();

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return delegate.chat(request);
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    long delay = backoffMs * (long) Math.pow(2, attempt - 1);
                    log.warn("[{}] Attempt {}/{} failed: {}. Retrying in {}ms...",
                            key, attempt, maxAttempts, e.getMessage(), delay);
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new ProviderException(key, "Interrupted during retry", e);
                    }
                }
            }
        }

        log.error("[{}] All {} attempts exhausted", key, maxAttempts);
        throw new ProviderException(key, "All retry attempts exhausted: " + lastException.getMessage(), lastException);
    }

    @Override
    public Flux<String> streamChat(ChatRequest request) {
        return delegate.streamChat(request).retry(maxAttempts - 1);
    }

    @Override
    public ProviderDescriptor getDescriptor() {
        return delegate.getDescriptor();
    }

    @Override
    public void initialize(ModelConfig config) {
        delegate.initialize(config);
    }

    @Override
    public boolean isReady() {
        return delegate.isReady();
    }
}
