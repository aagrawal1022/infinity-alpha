package com.infalpha.provider;

import com.infalpha.model.ChatRequest;
import com.infalpha.model.ChatResponse;
import com.infalpha.model.ModelConfig;
import com.infalpha.model.ProviderDescriptor;
import reactor.core.publisher.Flux;

/**
 * Strategy Pattern — The core abstraction for all LLM providers.
 * <p>
 * Every provider (OpenAI, Claude, Gemini, Groq, GitHub Models, etc.) implements
 * this interface. Clients interact with this contract only, never with
 * provider-specific code.
 * <p>
 * <b>Plugin Architecture:</b> New providers just implement this interface,
 * annotate with {@code @ProviderInfo} and {@code @Component}, and they're
 * auto-discovered at startup.
 */
public interface LlmProvider {

    /**
     * Send a synchronous chat completion request.
     */
    ChatResponse chat(ChatRequest request);

    /**
     * Send a streaming chat completion request (Server-Sent Events).
     */
    Flux<String> streamChat(ChatRequest request);

    /**
     * Get the provider descriptor (key, tier, display name, model prefixes).
     * Default implementation extracts from @ProviderInfo annotation.
     */
    default ProviderDescriptor getDescriptor() {
        var info = this.getClass().getAnnotation(com.infalpha.model.ProviderInfo.class);
        if (info == null) {
            throw new IllegalStateException(
                    getClass().getSimpleName() + " is missing @ProviderInfo annotation");
        }
        return ProviderDescriptor.from(info);
    }

    /**
     * Initialize the provider with its config from application.yml.
     * Called by the factory after construction.
     */
    void initialize(ModelConfig config);

    /**
     * Check if this provider has been initialized and is ready to serve.
     */
    boolean isReady();
}
