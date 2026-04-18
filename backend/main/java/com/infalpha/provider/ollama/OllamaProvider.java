package com.infalpha.provider.ollama;

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
import java.util.Map;
import java.util.stream.Collectors;

/**
 * FREE provider — Ollama (local inference).
 * <p>
 * HTTP-based (no SDK, runs locally).
 * No API key required.
 * Supports any model pulled into Ollama: llama3, mistral, phi3, codellama, etc.
 */
@Component
@ProviderInfo(
        key = "ollama",
        displayName = "Ollama (Local)",
        tier = ProviderTier.FREE,
        modelPrefixes = {"ollama/", "local/"}
)
public class OllamaProvider implements LlmProvider {

    private static final Logger log = LoggerFactory.getLogger(OllamaProvider.class);

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
        if (config.getBaseUrl() != null && !config.getBaseUrl().isBlank()) {
            this.webClient = webClientBuilder
                    .baseUrl(config.getBaseUrl())
                    .build();
            this.ready = true;
            log.info("[ollama] HTTP client initialized ({})", config.getBaseUrl());
        } else {
            log.warn("[ollama] No base URL, provider will not be ready");
        }
    }

    @Override
    public boolean isReady() {
        return ready;
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        long start = System.currentTimeMillis();
        String modelId = stripPrefix(request.getModel());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", modelId);
        body.put("messages", request.getMessages().stream()
                .map(m -> Map.of("role", m.getRole(), "content", m.getContent()))
                .collect(Collectors.toList()));
        body.put("stream", false);

        Map<String, Object> options = new LinkedHashMap<>();
        options.put("temperature", request.getTemperature());
        options.put("num_predict", request.getMaxTokens());
        options.put("top_p", request.getTopP());
        body.put("options", options);

        try {
            String rawResponse = webClient.post()
                    .uri("/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(config.getTimeoutSeconds()))
                    .block();

            JsonNode json = objectMapper.readTree(rawResponse);
            long latency = System.currentTimeMillis() - start;

            String content = json.path("message").path("content").asText("");
            int promptTokens = json.path("prompt_eval_count").asInt(0);
            int completionTokens = json.path("eval_count").asInt(0);

            return ChatResponse.builder()
                    .model(request.getModel())
                    .provider("ollama")
                    .tier(ProviderTier.FREE)
                    .content(content)
                    .usage(Usage.builder()
                            .promptTokens(promptTokens)
                            .completionTokens(completionTokens)
                            .totalTokens(promptTokens + completionTokens)
                            .build())
                    .latencyMs(latency)
                    .build();

        } catch (Exception e) {
            throw new com.infalpha.exception.ProviderException("ollama",
                    "Ollama request failed: " + e.getMessage(), e);
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
        body.put("stream", true);

        return webClient.post()
                .uri("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .timeout(Duration.ofSeconds(config.getTimeoutSeconds()))
                .mapNotNull(chunk -> {
                    try {
                        JsonNode json = objectMapper.readTree(chunk);
                        String content = json.path("message").path("content").asText();
                        return content.isEmpty() ? null : content;
                    } catch (Exception e) {
                        return null;
                    }
                });
    }

    private String stripPrefix(String model) {
        if (model.startsWith("ollama/")) return model.substring(7);
        if (model.startsWith("local/")) return model.substring(6);
        return model;
    }
}
