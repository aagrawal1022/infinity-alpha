package com.infalpha.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

/**
 * Unified chat request accepted by the gateway.
 * Works identically regardless of the underlying LLM provider.
 */
public class ChatRequest {

    @NotBlank(message = "Model must not be blank")
    private String model;

    @NotEmpty(message = "Messages must not be empty")
    @Valid
    private List<Message> messages;

    private Double temperature = 0.7;
    private Integer maxTokens = 1024;
    private Double topP = 1.0;
    private String conversationId;

    /** Optional tier hint — forces routing to FREE or PAID providers only */
    private ProviderTier tier;

    public ChatRequest() {}

    public ChatRequest(String model, List<Message> messages, Double temperature,
                       Integer maxTokens, Double topP, String conversationId, ProviderTier tier) {
        this.model = model;
        this.messages = messages;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.topP = topP;
        this.conversationId = conversationId;
        this.tier = tier;
    }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Integer getMaxTokens() { return maxTokens; }
    public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }

    public Double getTopP() { return topP; }
    public void setTopP(Double topP) { this.topP = topP; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public ProviderTier getTier() { return tier; }
    public void setTier(ProviderTier tier) { this.tier = tier; }

    public static ChatRequestBuilder builder() { return new ChatRequestBuilder(); }

    public static class ChatRequestBuilder {
        private String model;
        private List<Message> messages;
        private Double temperature = 0.7;
        private Integer maxTokens = 1024;
        private Double topP = 1.0;
        private String conversationId;
        private ProviderTier tier;

        public ChatRequestBuilder model(String v) { this.model = v; return this; }
        public ChatRequestBuilder messages(List<Message> v) { this.messages = v; return this; }
        public ChatRequestBuilder temperature(Double v) { this.temperature = v; return this; }
        public ChatRequestBuilder maxTokens(Integer v) { this.maxTokens = v; return this; }
        public ChatRequestBuilder topP(Double v) { this.topP = v; return this; }
        public ChatRequestBuilder conversationId(String v) { this.conversationId = v; return this; }
        public ChatRequestBuilder tier(ProviderTier v) { this.tier = v; return this; }
        public ChatRequest build() {
            return new ChatRequest(model, messages, temperature, maxTokens, topP, conversationId, tier);
        }
    }
}
