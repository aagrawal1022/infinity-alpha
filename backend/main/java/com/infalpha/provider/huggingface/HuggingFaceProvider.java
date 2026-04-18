package com.infalpha.provider.huggingface;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infalpha.model.*;
import com.infalpha.provider.LlmProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * FREE provider — HuggingFace Inference API.
 * <p>
 * HTTP-based (no official Java SDK exists).
 * Auth: HuggingFace token (free tier available).
 * Supports thousands of open-source models.
 * <p>
 * Model names should be prefixed with "hf/" or "huggingface/" for routing,
 * e.g., "hf/mistralai/Mistral-7B-Instruct-v0.3"
 */
@Component
@ProviderInfo(
        key = "huggingface",
        displayName = "HuggingFace",
        tier = ProviderTier.FREE,
        modelPrefixes = {"hf/", "huggingface/"}
)
public class HuggingFaceProvider implements LlmProvider {

    private static final Logger log = LoggerFactory.getLogger(HuggingFaceProvider.class);

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private ObjectMapper objectMapper;

    private WebClient webClient;
    private ModelConfig config;
    private boolean ready = false;

    @Override
    public void initialize(ModelConfig config) {
        this.config = config;
        if (config.getApiKey() != null && !config.getApiKey().isBlank()
                && config.getBaseUrl() != null) {
            this.webClient = webClientBuilder
                    .baseUrl(config.getBaseUrl())
                    .defaultHeader("Authorization", "Bearer " + config.getApiKey())
                    .build();
            this.ready = true;
            log.info("[huggingface] HTTP client initialized");
        } else {
            log.warn("[huggingface] Missing config, provider will not be ready");
        }
    }

    @Override
    public boolean isReady() {
        return ready;
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        long start = System.currentTimeMillis();

        // Strip "hf/" or "huggingface/" prefix to get the actual model ID
        String modelId = stripPrefix(request.getModel());

        // Build HF chat completion payload (OpenAI-compatible format)
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", modelId);
        body.put("messages", request.getMessages().stream()
                .map(m -> Map.of("role", m.getRole(), "content", m.getContent()))
                .collect(Collectors.toList()));
        body.put("max_tokens", request.getMaxTokens());
        body.put("temperature", request.getTemperature());

        try {
            String rawResponse = webClient.post()
                    .uri("/models/" + modelId + "/v1/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(config.getTimeoutSeconds()))
                    .block();

            JsonNode json = objectMapper.readTree(rawResponse);
            long latency = System.currentTimeMillis() - start;

            String content = json.path("choices").path(0).path("message").path("content").asText("");

            Usage usage = null;
            JsonNode usageNode = json.path("usage");
            if (!usageNode.isMissingNode()) {
                usage = Usage.builder()
                        .promptTokens(usageNode.path("prompt_tokens").asInt())
                        .completionTokens(usageNode.path("completion_tokens").asInt())
                        .totalTokens(usageNode.path("total_tokens").asInt())
                        .build();
            }

            return ChatResponse.builder()
                    .id(json.path("id").asText())
                    .model(request.getModel())
                    .provider("huggingface")
                    .tier(ProviderTier.FREE)
                    .content(content)
                    .usage(usage)
                    .latencyMs(latency)
                    .build();

        } catch (Exception e) {
            throw new com.infalpha.exception.ProviderException("huggingface",
                    "HuggingFace request failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Flux<String> streamChat(ChatRequest request) {
        String modelId = stripPrefix(request.getModel());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", modelId);
        body.put("messages", request.getMessages().stream()
                .map(m -> Map.of("role", m.getRole(), "content", m.getContent()))
                .collect(Collectors.toList()));
        body.put("max_tokens", request.getMaxTokens());
        body.put("stream", true);

        return webClient.post()
                .uri("/models/" + modelId + "/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .timeout(Duration.ofSeconds(config.getTimeoutSeconds()))
                .mapNotNull(chunk -> {
                    try {
                        JsonNode json = objectMapper.readTree(chunk);
                        JsonNode delta = json.path("choices").path(0).path("delta").path("content");
                        return delta.isMissingNode() ? null : delta.asText();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .onErrorMap(e -> new com.infalpha.exception.ProviderException("huggingface", "HuggingFace stream failed: " + e.getMessage(), e));
    }

    private String stripPrefix(String model) {
        if (model.startsWith("hf/")) return model.substring(3);
        if (model.startsWith("huggingface/")) return model.substring(12);
        return model;
    }
}
