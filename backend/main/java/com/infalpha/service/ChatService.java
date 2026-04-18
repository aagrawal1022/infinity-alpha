package com.infalpha.service;

import com.infalpha.event.ChatEvent;
import com.infalpha.event.ChatEventListener;
import com.infalpha.factory.ModelRegistry;
import com.infalpha.memory.ConversationMemory;
import com.infalpha.model.*;
import com.infalpha.provider.LlmProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Core orchestrator — tier-aware chat lifecycle:
 * <ol>
 *   <li>Resolve the correct provider via the Registry (respecting tier)</li>
 *   <li>Prepend conversation history (if conversationId is set)</li>
 *   <li>Delegate to the provider</li>
 *   <li>Notify observers (conversation memory, metrics)</li>
 * </ol>
 */
@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ModelRegistry registry;
    private final ConversationMemory memory;
    private final List<ChatEventListener> listeners;

    public ChatService(ModelRegistry registry,
                       ConversationMemory memory,
                       List<ChatEventListener> listeners) {
        this.registry = registry;
        this.memory = memory;
        this.listeners = listeners != null ? listeners : Collections.emptyList();
    }

    /**
     * Synchronous chat completion with tier-aware routing.
     */
    public ChatResponse chat(ChatRequest request) {
        // 1. Resolve provider (tier-aware)
        LlmProvider provider;
        if (request.getTier() != null) {
            provider = registry.findByModel(request.getModel(), request.getTier());
        } else {
            provider = registry.findByModel(request.getModel());
        }

        // 2. Prepend conversation history
        ChatRequest enrichedRequest = enrichWithHistory(request);

        // 3. Execute
        ChatResponse response = provider.chat(enrichedRequest);

        // 4. Notify observers
        notifyListeners(request, response);

        return response;
    }

    /**
     * Streaming chat completion (SSE) with tier-aware routing.
     */
    public Flux<String> streamChat(ChatRequest request) {
        LlmProvider provider;
        if (request.getTier() != null) {
            provider = registry.findByModel(request.getModel(), request.getTier());
        } else {
            provider = registry.findByModel(request.getModel());
        }
        ChatRequest enrichedRequest = enrichWithHistory(request);
        return provider.streamChat(enrichedRequest);
    }

    /**
     * List all available models/providers, grouped by tier.
     */
    public Map<String, Object> listModels(ProviderTier tier) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (tier == null) {
            // Return all, grouped
            result.put("free", registry.getDescriptors(ProviderTier.FREE).stream()
                    .map(this::descriptorToMap).collect(Collectors.toList()));
            result.put("paid", registry.getDescriptors(ProviderTier.PAID).stream()
                    .map(this::descriptorToMap).collect(Collectors.toList()));
        } else {
            result.put(tier.name().toLowerCase(), registry.getDescriptors(tier).stream()
                    .map(this::descriptorToMap).collect(Collectors.toList()));
        }

        return result;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Map<String, Object> descriptorToMap(ProviderDescriptor desc) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("key", desc.getKey());
        info.put("displayName", desc.getDisplayName());
        info.put("tier", desc.getTier().name());
        info.put("modelPrefixes", desc.getModelPrefixes());
        return info;
    }

    private ChatRequest enrichWithHistory(ChatRequest request) {
        if (request.getConversationId() == null) {
            return request;
        }

        List<Message> history = memory.getHistory(request.getConversationId());
        if (history.isEmpty()) {
            return request;
        }

        List<Message> merged = new ArrayList<>();

        request.getMessages().stream()
                .filter(m -> "system".equals(m.getRole()))
                .forEach(merged::add);

        merged.addAll(history);

        request.getMessages().stream()
                .filter(m -> !"system".equals(m.getRole()))
                .forEach(merged::add);

        return ChatRequest.builder()
                .model(request.getModel())
                .messages(merged)
                .temperature(request.getTemperature())
                .maxTokens(request.getMaxTokens())
                .topP(request.getTopP())
                .conversationId(request.getConversationId())
                .tier(request.getTier())
                .build();
    }

    private void notifyListeners(ChatRequest request, ChatResponse response) {
        ChatEvent event = ChatEvent.builder()
                .request(request)
                .response(response)
                .build();

        for (ChatEventListener listener : listeners) {
            try {
                listener.onChatCompleted(event);
            } catch (Exception e) {
                log.warn("ChatEventListener failed: {}", e.getMessage());
            }
        }
    }
}
