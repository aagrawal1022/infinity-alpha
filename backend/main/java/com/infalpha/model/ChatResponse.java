package com.infalpha.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Unified chat response returned by the gateway.
 * Normalized across all LLM providers.
 */
public class ChatResponse {

    private String id;
    private String model;
    /** Dynamic provider key (e.g. "openai", "groq", "github-models") */
    private String provider;
    /** Provider tier */
    private ProviderTier tier;
    private String content;
    private Usage usage;
    private long latencyMs;
    private Instant createdAt;

    public ChatResponse() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
    }

    public ChatResponse(String id, String model, String provider, ProviderTier tier,
                        String content, Usage usage, long latencyMs, Instant createdAt) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.model = model;
        this.provider = provider;
        this.tier = tier;
        this.content = content;
        this.usage = usage;
        this.latencyMs = latencyMs;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public ProviderTier getTier() { return tier; }
    public void setTier(ProviderTier tier) { this.tier = tier; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Usage getUsage() { return usage; }
    public void setUsage(Usage usage) { this.usage = usage; }

    public long getLatencyMs() { return latencyMs; }
    public void setLatencyMs(long latencyMs) { this.latencyMs = latencyMs; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public static ChatResponseBuilder builder() { return new ChatResponseBuilder(); }

    public static class ChatResponseBuilder {
        private String id;
        private String model;
        private String provider;
        private ProviderTier tier;
        private String content;
        private Usage usage;
        private long latencyMs;
        private Instant createdAt;

        public ChatResponseBuilder id(String v) { this.id = v; return this; }
        public ChatResponseBuilder model(String v) { this.model = v; return this; }
        public ChatResponseBuilder provider(String v) { this.provider = v; return this; }
        public ChatResponseBuilder tier(ProviderTier v) { this.tier = v; return this; }
        public ChatResponseBuilder content(String v) { this.content = v; return this; }
        public ChatResponseBuilder usage(Usage v) { this.usage = v; return this; }
        public ChatResponseBuilder latencyMs(long v) { this.latencyMs = v; return this; }
        public ChatResponseBuilder createdAt(Instant v) { this.createdAt = v; return this; }
        public ChatResponse build() {
            return new ChatResponse(id, model, provider, tier, content, usage, latencyMs, createdAt);
        }
    }
}
