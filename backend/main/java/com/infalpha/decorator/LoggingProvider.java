package com.infalpha.decorator;

import com.infalpha.model.*;
import com.infalpha.provider.LlmProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

/**
 * Decorator Pattern — Adds structured logging to any LlmProvider.
 */
public class LoggingProvider implements LlmProvider {

    private static final Logger log = LoggerFactory.getLogger(LoggingProvider.class);

    private final LlmProvider delegate;

    public LoggingProvider(LlmProvider delegate) {
        this.delegate = delegate;
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        String key = getDescriptor().getKey();
        log.info("[{}] ➜ Chat request | model={} | messages={} | maxTokens={}",
                key, request.getModel(), request.getMessages().size(), request.getMaxTokens());

        ChatResponse response = delegate.chat(request);

        log.info("[{}] ✓ Chat response | model={} | tokens={} | latency={}ms",
                key, response.getModel(),
                response.getUsage() != null ? response.getUsage().getTotalTokens() : "N/A",
                response.getLatencyMs());

        return response;
    }

    @Override
    public Flux<String> streamChat(ChatRequest request) {
        String key = getDescriptor().getKey();
        log.info("[{}] ➜ Stream request | model={} | messages={}",
                key, request.getModel(), request.getMessages().size());

        return delegate.streamChat(request)
                .doOnComplete(() -> log.info("[{}] ✓ Stream completed", key))
                .doOnError(e -> log.error("[{}] ✗ Stream error: {}", key, e.getMessage()));
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
