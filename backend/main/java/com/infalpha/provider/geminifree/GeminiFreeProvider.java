package com.infalpha.provider.geminifree;

import com.google.genai.Client;
import com.google.genai.types.*;
import com.infalpha.model.*;
import com.infalpha.provider.LlmProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * FREE provider — Google AI Studio (free Gemini API key).
 * Same SDK as paid Gemini, different config and model defaults.
 */
@Component
@ProviderInfo(key = "gemini-free", displayName = "Google AI Studio (Free)", tier = ProviderTier.FREE, modelPrefixes = {
        "gemini-2.0-flash", "gemini-1.5-flash", "gemini-2.0", "gemini-2.5-flash" })
public class GeminiFreeProvider implements LlmProvider {

    private static final Logger log = LoggerFactory.getLogger(GeminiFreeProvider.class);

    private Client client;
    private ModelConfig config;
    private boolean ready = false;

    @Override
    public void initialize(ModelConfig config) {
        this.config = config;
        if (config.getApiKey() != null && !config.getApiKey().isBlank()) {
            this.client = Client.builder()
                    .apiKey(config.getApiKey())
                    .build();
            this.ready = true;
            log.info("[gemini-free] SDK initialized (free tier)");
        } else {
            log.warn("[gemini-free] No API key, provider will not be ready");
        }
    }

    @Override
    public boolean isReady() {
        return ready;
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        long start = System.currentTimeMillis();

        List<Content> contents = new ArrayList<>();
        Optional<String> systemInstruction = Optional.empty();

        for (Message msg : request.getMessages()) {
            if ("system".equals(msg.getRole())) {
                systemInstruction = Optional.of(msg.getContent());
            } else {
                String role = "assistant".equals(msg.getRole()) ? "model" : "user";
                contents.add(Content.builder()
                        .role(role)
                        .parts(List.of(Part.builder().text(msg.getContent()).build()))
                        .build());
            }
        }

        GenerateContentConfig.Builder configBuilder = GenerateContentConfig.builder()
                .temperature(request.getTemperature().floatValue())
                .maxOutputTokens(request.getMaxTokens())
                .topP(request.getTopP().floatValue());

        systemInstruction.ifPresent(s -> configBuilder.systemInstruction(Content.builder()
                .parts(List.of(Part.builder().text(s).build()))
                .build()));

        GenerateContentResponse response = client.models.generateContent(
                request.getModel(), contents, configBuilder.build());

        long latency = System.currentTimeMillis() - start;
        String text = response.text() != null ? response.text() : "";

        Usage usage = extractUsage(response);

        return ChatResponse.builder()
                .model(request.getModel())
                .provider("gemini-free")
                .tier(ProviderTier.FREE)
                .content(text)
                .usage(usage)
                .latencyMs(latency)
                .build();
    }

    @Override
    public Flux<String> streamChat(ChatRequest request) {
        return Flux.create(sink -> {
            try {
                List<Content> contents = new ArrayList<>();
                for (Message msg : request.getMessages()) {
                    if (!"system".equals(msg.getRole())) {
                        String role = "assistant".equals(msg.getRole()) ? "model" : "user";
                        contents.add(Content.builder()
                                .role(role)
                                .parts(List.of(Part.builder().text(msg.getContent()).build()))
                                .build());
                    }
                }

                GenerateContentConfig streamConfig = GenerateContentConfig.builder()
                        .temperature(request.getTemperature().floatValue())
                        .maxOutputTokens(request.getMaxTokens())
                        .topP(request.getTopP().floatValue())
                        .build();

                client.models.generateContentStream(request.getModel(), contents, streamConfig)
                        .forEach(chunk -> {
                            if (chunk.text() != null) {
                                sink.next(chunk.text());
                            }
                        });
                sink.complete();
            } catch (Exception e) {
                sink.error(new com.infalpha.exception.ProviderException("gemini-free", e.getMessage(), e));
            }
        });
    }

    private Usage extractUsage(GenerateContentResponse response) {
        var metadataOpt = response.usageMetadata();
        if (metadataOpt == null || metadataOpt.isEmpty())
            return null;
        try {
            var um = metadataOpt.get();
            int prompt = um.promptTokenCount().orElse(0);
            int completion = um.candidatesTokenCount().orElse(0);
            return Usage.builder()
                    .promptTokens(prompt)
                    .completionTokens(completion)
                    .totalTokens(prompt + completion)
                    .build();
        } catch (Exception e) {
            log.debug("Could not extract usage metadata: {}", e.getMessage());
            return null;
        }
    }
}
